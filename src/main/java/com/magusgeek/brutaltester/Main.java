package com.magusgeek.brutaltester;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.magusgeek.brutaltester.util.Mutable;

public class Main {
    private static final Log LOG = LogFactory.getLog(Main.class);
    
    private static PlayerStats[] playerStats;
    private static int t;
    private static int finished = 0;

    public static void main(String[] args) {
        try {
            Options options = new Options();

            options.addOption("h", false, "Print the help")
                   .addOption("v", false, "Verbose mode. Spam incoming.")
                   .addOption("n", true, "Number of games to play. Default 1.")
                   .addOption("t", true, "Number of thread to spawn for the games. Default 1.")
                   .addOption("r", true, "Required. Referee command line.")
                   .addOption("p1", true, "Required. Player 1 command line.")
                   .addOption("p2", true, "Required. Player 2 command line.")
                   .addOption("p3", true, "Player 3 command line.")
                   .addOption("p4", true, "Player 4 command line.");

            CommandLine cmd = new DefaultParser().parse(options, args);

            // Need help ?
            if (cmd.hasOption("h") || !cmd.hasOption("r") || !cmd.hasOption("p1") || !cmd.hasOption("p2")) {
                new HelpFormatter().printHelp("-r <referee command line> -p1 <player1 command line> -p2 <player2 command line> -p3 <player3 command line> -p4 <player4 command line> [-v -n <games> -t <thread>]", options);
                System.exit(0);
            }

            // Verbose mode
            if (cmd.hasOption("v")) {
                Configurator.setRootLevel(Level.ALL);
                LOG.info("Verbose mode activated");
            }

            // Referee command line
            String refereeCmd = cmd.getOptionValue("r");
            LOG.info("Referee command line: " + refereeCmd);

            // Players command lines
            List<String> playersCmd = new ArrayList<>();
            for (int i = 1; i <= 4; ++i) {
                String value = cmd.getOptionValue("p" + i);

                if (value != null) {
                    playersCmd.add(value);
                    LOG.info("Player " + i + " command line: " + value);
                }
            }

            // Games count
            int n = 1;
            try {
                n = Integer.valueOf(cmd.getOptionValue("n"));
            } catch (Exception exception) {

            }
            LOG.info("Number of games to play: " + n);

            // Thread count
            t = 1;
            try {
                t = Integer.valueOf(cmd.getOptionValue("t"));
            } catch (Exception exception) {

            }
            LOG.info("Number of threads to spawn: " + t);

            // Prepare stats objects
            playerStats = new PlayerStats[playersCmd.size()];
            for (int i = 0; i < playersCmd.size(); ++i) {
                playerStats[i] = new PlayerStats();
            }

            Mutable<Integer> count = new Mutable<>(0);
            
            // Start the threads
            for (int i = 0; i < t; ++i) {
                new GameThread(i + 1, refereeCmd, playersCmd, count, playerStats, n).start();
            }
        } catch (Exception exception) {
            LOG.fatal("cg-brutaltester failed to start", exception);
            System.exit(1);
        }
    }
    
    public static void finish() {
        synchronized (playerStats) {
            finished += 1;
            
            if (finished >= t) {
                System.out.println("*** End of games ***");
                for (int i = 0; i < playerStats.length; ++i) {
                    System.out.println("*** Statistics for player " + (i + 1) + ":");
                    playerStats[i].print();
                }
            }
        }
    }
}
