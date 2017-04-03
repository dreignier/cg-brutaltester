package com.magusgeek.brutaltester;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.magusgeek.brutaltester.util.Mutable;

public class GameThread extends Thread {
    private static final Log LOG = LogFactory.getLog(GameThread.class);
    
    private int id;
    private Mutable<Integer> count;
    private PlayerStats[] playerStats;
    private int n;
    private List<BrutalProcess> players;
    private BrutalProcess referee;
    
    private ProcessBuilder refereeBuilder;
    private List<ProcessBuilder> playerBuilders;

    public GameThread(int id, String refereeCmd, List<String> playersCmd, Mutable<Integer> count, PlayerStats[] playerStats, int n) {
        this.id = id;
        this.count = count;
        this.playerStats = playerStats;
        this.n = n;
        
        refereeBuilder = new ProcessBuilder(refereeCmd.split(" "));
        playerBuilders = new ArrayList<>();
        for (String cmd : playersCmd) {
            playerBuilders.add(new ProcessBuilder(cmd.split(" ")));
        }
    }
    
    private void log(String message) {
        System.out.println(message);
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
                referee = new BrutalProcess(refereeBuilder.start());
                
                // Spawn players process
                players = new ArrayList<>();
                for (ProcessBuilder builder : playerBuilders) {
                    players.add(new BrutalProcess(builder.start()));
                }
                
                String line = referee.getIn().nextLine();
                log("Referee: " + line);
          
                while (!line.startsWith("###End")) {
                    if (line.startsWith("###Input")) {
                        // Read all lines from the referee until next command and give it to the targeted process
                        PrintStream outputStream = players.get(Character.getNumericValue(line.charAt(9))).getOut();
                        
                        line = referee.getIn().nextLine();
                        while (!line.startsWith("###")) {
                            log("Referee: " + line);
                            
                            outputStream.println(line.getBytes(StandardCharsets.UTF_8));
                            line = referee.getIn().nextLine();
                        }
                        
                        outputStream.flush();
                        referee.clearErrorStream();
                    } else if (line.startsWith("###Output")) {
                        // Read x lines from the targeted process and give to the referee
                        int target = Character.getNumericValue(line.charAt(10));
                        int x =  Character.getNumericValue(line.charAt(12));
                        
                        BrutalProcess player = players.get(target);
                        player.clearErrorStream();
                        
                        for (int i = 0; i < x; ++i) {
                            String playerLine = player.getIn().nextLine();
                            log("Player " + target + ": " + playerLine);
                            referee.getOut().println(playerLine.getBytes(StandardCharsets.UTF_8));
                        }
                    
                        referee.getOut().flush();
                        player.clearErrorStream();
                    }
                }
                
                // End of the game
                String[] params = line.split(" ");
                for (int i = 1; i < params.length; ++i) {
                    for (char c : params[i].toCharArray()) {
                        playerStats[Character.getNumericValue(c)].add(i - 1);
                    }
                }
                
                LOG.info("End of game " + game);
            } catch (Exception exception) {
                LOG.error("Exception in GameThread " + id, exception);
            } finally {
                destroyAll();
            }
        }
    }
    
    private void destroyAll() {
        try {
            if (players != null) {
                for (BrutalProcess player: players) {
                    player.destroy();
                }
            }
            
            if (referee != null) {
                referee.destroy();
            }
        } catch (Exception exception) {
            LOG.error("Unable to destroy all");
        }
    }

}
