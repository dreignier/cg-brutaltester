package com.magusgeek.brutaltester;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.magusgeek.brutaltester.util.Mutable;
import com.magusgeek.brutaltester.util.SeedGenerator;

public class OldGameThread extends Thread {

    private static final Log LOG = LogFactory.getLog(OldGameThread.class);

    private Mutable<Integer> count;
    private PlayerStats playerStats;
    private int n;
    private List<BrutalProcess> players;
    private BrutalProcess referee;
    private Path logs;
    private PrintStream logsWriter;

    private ProcessBuilder refereeBuilder;
    private List<ProcessBuilder> playerBuilders;
    private int game;
    private boolean swap;

    public OldGameThread(int id, String refereeCmd, List<String> playersCmd, Mutable<Integer> count, PlayerStats playerStats, int n, Path logs, boolean swap) {
        super("GameThread " + id);
        this.count = count;
        this.playerStats = playerStats;
        this.n = n;
        this.logs = logs;
        this.swap = swap;

        refereeBuilder = new ProcessBuilder(refereeCmd.split(" "));
        playerBuilders = new ArrayList<>();
        for (String cmd : playersCmd) {
            playerBuilders.add(new ProcessBuilder(cmd.split(" ")));
        }
    }

    public void log(String message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("[Game " + game + "] " + message);
        }

        if (logsWriter != null) {
            logsWriter.println(message);
        }
    }

    public void run() {
        while (true) {
            game = 0;
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
                if (this.logs != null) {
                    // Open logs stream
                    logsWriter = new PrintStream(this.logs + "/game" + game + ".log");
                }

                // Spawn referee process
                referee = new BrutalProcess(refereeBuilder.start());

                int[] seedRotate = SeedGenerator.getSeed(playerBuilders.size());
                int rotate = swap ? seedRotate[1] : 0;
                // Spawn players process
                players = new ArrayList<>();
                for (int i = 0; i < playerBuilders.size(); i++) {
                    players.add(new BrutalProcess(playerBuilders.get((i + rotate) % playerBuilders.size()).start()));
                }

                if (swap) {
                    referee.getOut().println("###Seed " + seedRotate[0]);
                }
                else if (SeedGenerator.repeteableTests){
                    referee.getOut().println("###Seed " + SeedGenerator.nextSeed());
                }
                referee.getOut().println("###Start " + players.size());
                referee.getOut().flush();

                String line = referee.getIn().nextLine();
                log("Referee: " + line);

                while (!line.startsWith("###End")) {
                    referee.clearErrorStream(this, "Referee error: ");

                    if (line.startsWith("###Input")) {
                        // Read all lines from the referee until next command and give it to the targeted process
                        PrintStream outputStream = players.get(Character.getNumericValue(line.charAt(9))).getOut();
                        line = referee.getIn().nextLine();
                        while (!line.startsWith("###")) {
                            log("Referee: " + line);

                            outputStream.println(line);
                            line = referee.getIn().nextLine();
                        }

                        outputStream.flush();
                    } else if (line.startsWith("###Output")) {
                        // Read x lines from the targeted process and give to the referee
                        String[] parts = line.split(" ");
                        int target = Integer.parseInt(parts[1]);
                        int x = Integer.parseInt(parts[2]);

                        BrutalProcess player = players.get(target);
                        player.clearErrorStream(this, "Player " + target + " error: ");

                        for (int i = 0; i < x; ++i) {
                            String playerLine = player.getIn().nextLine();
                            log("Player " + target + ": " + playerLine);
                            referee.getOut().println(playerLine);
                        }

                        referee.getOut().flush();

                        line = referee.getIn().nextLine();
                    } else if (line.startsWith("###Error")) {
                        int target = Character.getNumericValue(line.charAt(9));
                        LOG.warn("Error for player " + target + " in game " + game + ": " + line.substring(9));
                    }
                }

                // End of the game
                // unswap the positions to declare the correct winner
                String unrotated = "";
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c >= '0' && c <= '9') {
                        c -= '0';
                        c += rotate;
                        c %= players.size();
                        c += '0';
                    }
                    unrotated += c;
                }
                line = unrotated;

                log("Referee: " + line);
                playerStats.add(line);

                LOG.info("End of game " + game + ": " + line.substring(7) + "\t" + playerStats);
            } catch (Exception exception) {
                LOG.error("Exception in game " + game, exception);
            } finally {
                destroyAll();
            }
        }
    }

    private void destroyAll() {
        try {
            if (players != null) {
                for (BrutalProcess player : players) {
                    player.destroy();
                }
            }

            if (referee != null) {
                referee.destroy();
            }
        } catch (Exception exception) {
            LOG.error("Unable to destroy all");
        }

        if (logsWriter != null) {
            logsWriter.close();
            logsWriter = null;
        }
    }

}
