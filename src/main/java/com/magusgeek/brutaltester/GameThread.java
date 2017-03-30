package com.magusgeek.brutaltester;

import java.util.*;
import java.util.stream.Collectors;

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
                boolean input = false;
                int target = 0;
                
                while (!line.startsWith("###End")) {
                    if (line.startsWith("###Input")) {
                        // Read all lines from the referee until next command and give it to the targeted process
                    } else if (line.startsWith("###Output")) {
                        // Read x liens from the targeted process and give to the referee
                    }
                }
                
                // End of the game
                String[] params = line.split(" ");
                for (int i = 1; i < params.length; ++i) {
                    for (char c : params[i].toCharArray()) {
                        playerStats[Character.getNumericValue(c)].add(i - 1);
                    }
                }
                
                destroyAll();
            } catch (Exception exception) {
                LOG.error("Exception in GameThread " + id, exception);
                destroyAll();
            }
        }
    }
    
    private void destroyAll() {
        if (scanners != null) {
            scanners.forEach(s -> s.close());
        
        if (refereeScanner != null) {
            refereeScanner.close();
        }
        
        if (players != null) {
            players.forEach(p -> p.destroy());
        }
        
        if (referee != null) {
            referee.destroy();
        }
    }

}
