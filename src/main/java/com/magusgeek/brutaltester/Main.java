package com.magusgeek.brutaltester;

import com.magusgeek.brutaltester.util.Mutable;
import com.magusgeek.brutaltester.util.SeedGenerator;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class);

    private static PlayerStats playerStats;
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
                   .addOption("p4", true, "Player 4 command line.")
                   .addOption("l", true, "A directory for games logs")
                   .addOption("s", false, "Swap player positions")
                   .addOption("i", true, "Initial seed. For repeatable tests")
                   .addOption("o", false, "Old mode");

            CommandLine cmd = new DefaultParser().parse(options, args);

            // Need help ?
            if (cmd.hasOption("h") || !cmd.hasOption("r") || !cmd.hasOption("p1") || !cmd.hasOption("p2")) {
                new HelpFormatter().printHelp("-r <referee command line> -p1 <player1 command line> -p2 <player2 command line> -p3 <player3 command line> -p4 <player4 command line> [-o -v -n <games> -t <thread>]", options);
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

            // Logs directory
            Path logs = null;
            if (cmd.hasOption("l")) {
                logs = FileSystems.getDefault().getPath(cmd.getOptionValue("l"));
                if (!Files.isDirectory(logs)) {
                    throw new NotDirectoryException("Given path for the logs directory is not a directory: " + logs);
                }
            }

            boolean swap = cmd.hasOption("s");
            //Seed Initialization
            if (cmd.hasOption("i")){
                long newSeed = Integer.valueOf(cmd.getOptionValue("i"));
                SeedGenerator.initialSeed(newSeed);
                LOG.info("Initial Seed: " + newSeed);
            }
            // Prepare stats objects
            playerStats = new PlayerStats(playersCmd.size());
            Mutable<Integer> count = new Mutable<>(0);

            // Start the threads
            for (int i = 0; i < t; ++i) {
            	if (cmd.hasOption("o")) {
            		new OldGameThread(i + 1, refereeCmd, playersCmd, count, playerStats, n, logs, swap).start();
            	} else {
            		new GameThread(i + 1, refereeCmd, playersCmd, count, playerStats, n, logs, swap).start();
            	}
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
                LOG.info("*** End of games ***");
                playerStats.print();
            }
        }
    }
}
