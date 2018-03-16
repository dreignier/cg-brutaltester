package com.magusgeek.brutaltester;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.magusgeek.brutaltester.util.Mutable;

public class GameThread extends Thread {
	private static final Log LOG = LogFactory.getLog(GameThread.class);

	private Mutable<Integer> count;
	private PlayerStats stats;
	private int n;
	private BrutalProcess referee;
	private Path logs;
	private PrintStream logsWriter;
	private int game;
	private String command[];

	public GameThread(int id, String refereeCmd, List<String> playersCmd, Mutable<Integer> count, PlayerStats stats, int n, Path logs) {
		super("GameThread " + id);
		this.count = count;
		this.stats = stats;
		this.n = n;
		this.logs = logs;
		
		command = new String[playersCmd.size() * 2 + (logs != null ? 2 : 0)];
		
		for (int i = 0; i < playersCmd.size(); ++i) {
		    command[i*2] = "-p" + (i + 1);
		    command[i*2 + 1] = playersCmd.get(i);
		}
		
		if (logs != null) {
		  command[playersCmd.size() * 2 + 2] = "-l";
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
              if (logs != null) {
                command[command.length - 1] = new StringBuilder(logs.toString()).append("/game").append(game).append(".json").toString();
              }
              
              referee = new BrutalProcess(Runtime.getRuntime().exec(command));
              
              boolean error = false;
              StringBuilder data = new StringBuilder();
              
              try (Scanner in = referee.getIn()) {
                
              }
              
              try (Scanner in = new Scanner(referee.getError())) {
                if (in.hasNext()) {
                  error = true;
                  LOG.error("Error during game " + game);
                  
                  while (in.hasNext()) {
                    LOG.error(in.nextLine());
                  }
                }
              }
              
              if (error) {
                LOG.error("If you want to replay this game, use the following command line:");
                LOG.error(String.join(" ", command) + "-d " + data);
              }
              
            } catch (Exception exception) {
                LOG.error("Exception in game " + game, exception);
            } finally {
                destroyAll();
            }

//            try {
//            	if (this.logs != null) {
//                    // Open logs stream
//                    logsWriter = new PrintStream(this.logs + "/game" + game + ".log");
//                }
//
//                // Spawn referee process
//                referee = new BrutalProcess(builder.start());
//
//                if (swap) {
//                    referee.getOut().println("###Seed " + seedRotate[0]);
//                }
//                else if (SeedGenerator.repeteableTests){
//                    referee.getOut().println("###Seed " + SeedGenerator.nextSeed());
//                }
//                referee.getOut().println("###Start " + players.size());
//                referee.getOut().flush();
//
//                String line = referee.getIn().nextLine();
//                log("Referee: " + line);
//
//                while (!line.startsWith("###End")) {
//                    referee.clearErrorStream(this, "Referee error: ");
//
//                    if (line.startsWith("###Input")) {
//                        // Read all lines from the referee until next command and give it to the targeted process
//                        PrintStream outputStream = players.get(Character.getNumericValue(line.charAt(9))).getOut();
//                        line = referee.getIn().nextLine();
//                        while (!line.startsWith("###")) {
//                            log("Referee: " + line);
//
//                            outputStream.println(line);
//                            line = referee.getIn().nextLine();
//                        }
//
//                        outputStream.flush();
//                    } else if (line.startsWith("###Output")) {
//                        // Read x lines from the targeted process and give to the referee
//                        String[] parts = line.split(" ");
//                        int target = Integer.parseInt(parts[1]);
//                        int x = Integer.parseInt(parts[2]);
//
//                        BrutalProcess player = players.get(target);
//                        player.clearErrorStream(this, "Player " + target + " error: ");
//
//                        for (int i = 0; i < x; ++i) {
//                            String playerLine = player.getIn().nextLine();
//                            log("Player " + target + ": " + playerLine);
//                            referee.getOut().println(playerLine);
//                        }
//
//                        referee.getOut().flush();
//
//                        line = referee.getIn().nextLine();
//                    } else if (line.startsWith("###Error")) {
//                        int target = Character.getNumericValue(line.charAt(9));
//                        LOG.warn("Error for player " + target + " in game " + game + ": " + line.substring(9));
//                    }
//                }
//
//                // End of the game
//                // unswap the positions to declare the correct winner
//                String unrotated = "";
//                for (int i = 0; i < line.length(); i++) {
//                    char c = line.charAt(i);
//                    if (c >= '0' && c <= '9') {
//                        c -= '0';
//                        c += rotate;
//                        c %= players.size();
//                        c += '0';
//                    }
//                    unrotated += c;
//                }
//                line = unrotated;
//
//                log("Referee: " + line);
//                playerStats.add(line);
//
//                LOG.info("End of game " + game + ": " + line.substring(7) + "\t" + playerStats);
//            } catch (Exception exception) {
//                LOG.error("Exception in game " + game, exception);
//            } finally {
//                destroyAll();
//            }
        }
    }
	
	private void destroyAll() {
        try {
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
