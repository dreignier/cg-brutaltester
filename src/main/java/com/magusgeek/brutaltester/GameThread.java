package com.magusgeek.brutaltester;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.CloseAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.magusgeek.brutaltester.util.Mutable;

public class GameThread extends Thread {
    private static final Log LOG = LogFactory.getLog(GameThread.class);
    
    private int id;
    private String refereeCmd;
    private List<String> playersCmd;
    private Mutable<Integer> count;
    private PlayerStats[] playerStats;
    private int n;
    private List<Process> players;
    private Process referee;
    private List<Scanner> scanners;
    private Scanner refereeScanner;

    public GameThread(int id, String refereeCmd, List<String> playersCmd, Mutable<Integer> count, PlayerStats[] playerStats, int n) {
        this.id = id;
        this.refereeCmd = refereeCmd;
        this.playersCmd = playersCmd;
        this.count = count;
        this.playerStats = playerStats;
        this.n = n;
    }
    
    public void run() {
        while (true) {
            int game = 0;
            synchronized (count) {
                if (count.get() < n) {
                    game = count.get() + 1;
                    count.set(game);
                }
            }
            
            if (game == 0) {
                // End of this thread
                Main.finish();
                break;
            }
            
            try {
                // Spawn referee process
                referee = new ProcessBuilder(refereeCmd.split(" ")).start();
                
                // Spawn players process
                players = new ArrayList<>();
                for (String cmd : playersCmd) {
                    players.add(new ProcessBuilder(cmd.split(" ")).start());
                }
                
                // Plug scanners
                refereeScanner = new Scanner(referee.getInputStream());
                scanners = new ArrayList<>();
                for (Process process : players) {
                    scanners.add(new Scanner(process.getInputStream()));
                }
                
                String line = refereeScanner.nextLine();
                
                while (!line.startsWith("###End")) {
                    if (line.startsWith("###Input")) {
                        // Read all lines from the referee until next command and give it to the targeted process
                        OutputStream outputStream = players.get(Character.getNumericValue(line.charAt(9))).getOutputStream();
                        
                        line = refereeScanner.nextLine();
                        while (!line.startsWith("###")) {
                            outputStream.write(line.getBytes(StandardCharsets.UTF_8));
                            outputStream.write('\n');
                        }
                        
                        outputStream.flush();
                        clearErrorStream(referee);
                    } else if (line.startsWith("###Output")) {
                        // Read x lines from the targeted process and give to the referee
                        int target = Character.getNumericValue(line.charAt(10));
                        int x =  Character.getNumericValue(line.charAt(12));
                        
                        Process player = players.get(target);
                        OutputStream outputStream = referee.getOutputStream();
                        Scanner scanner = scanners.get(target);
                        
                        for (int i = 0; i < x; ++i) {
                            outputStream.write(scanner.nextLine().getBytes(StandardCharsets.UTF_8));
                            outputStream.write('\n');
                        }
                    
                        outputStream.flush();
                        clearErrorStream(player);
                    }
                }
                
                // End of the game
                String[] params = line.split(" ");
                for (int i = 1; i < params.length; ++i) {
                    for (char c : params[i].toCharArray()) {
                        playerStats[Character.getNumericValue(c)].add(i - 1);
                    }
                }
                
                
            } catch (Exception exception) {
                LOG.error("Exception in GameThread " + id, exception);
            } finally {
                destroyAll();
            }
        }
    }
    
    private void destroyAll() {
        if (scanners != null) {
            scanners.forEach(Scanner::close);
        }
        
        if (refereeScanner != null) {
            refereeScanner.close();
        }
        
        if (players != null) {
            players.forEach(Process::destroy);
        }
        
        if (referee != null) {
            referee.destroy();
        }
    }
        
    private void clearErrorStream(Process process) throws IOException {
        InputStream errorStream = process.getErrorStream();
        
        while(errorStream.available() != 0) {
            errorStream.read();
        }
    }

}
