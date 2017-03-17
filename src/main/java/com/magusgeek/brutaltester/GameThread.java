package com.magusgeek.brutaltester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                players = new ArrayList<Process>();
                for (String cmd : playersCmd) {
                    players.add(new ProcessBuilder(cmd.split(" ")).start());
                }
                
                
            } catch (Exception exception) {
                LOG.error("Exception in GameThread " + id, exception);
                destroyAll();
            }
        }
    }
    
    private void destroyAll() {
        players.forEach(p -> p.destroy());
        
        if (referee != null) {
            referee.destroy();
        }
    }

}
