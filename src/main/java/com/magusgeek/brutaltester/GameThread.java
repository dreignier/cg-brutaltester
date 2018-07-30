package com.magusgeek.brutaltester;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.magusgeek.brutaltester.util.Mutable;
import com.magusgeek.brutaltester.util.SeedGenerator;

public class GameThread extends Thread {
	private static final Log LOG = LogFactory.getLog(GameThread.class);

	private Mutable<Integer> count;
	private PlayerStats stats;
	private int n;
	private BrutalProcess referee;
	private Path logs;
	private int game;
	private String command[];
	private int playersCount;
	private List<String> playersCmd;
	private StringBuilder data = new StringBuilder();
	private boolean swap;
	private int pArgIdx[];
	private int refereeInputIdx;

	public GameThread(int id, String refereeCmd, List<String> playersCmd, Mutable<Integer> count, PlayerStats stats,
			int n, Path logs, boolean swap) {
		super("GameThread-" + id);
		this.count = count;
		this.stats = stats;
		this.n = n;
		this.logs = logs;
		this.swap = swap;
		this.playersCount = playersCmd.size();
		this.playersCmd = playersCmd;
		this.pArgIdx = new int[playersCount];
		boolean haveSeedArgs = swap || SeedGenerator.repeteableTests;

		String[] splitted = refereeCmd.split(" ");

		command = new String[splitted.length + playersCount * 2 + (logs != null ? 2 : 0) + (haveSeedArgs ? 2 : 0)];

		for (int i = 0; i < splitted.length; ++i) {
			command[i] = splitted[i];
		}

		for (int i = 0; i < playersCount; ++i) {
			pArgIdx[i] = splitted.length + i * 2 + 1;
			command[splitted.length + i * 2] = "-p" + (i + 1);
			command[splitted.length + i * 2 + 1] = playersCmd.get(i);
		}
		
		if (haveSeedArgs) {
			this.n *= playersCount;
			refereeInputIdx = splitted.length + playersCount * 2 + 1;
			command[refereeInputIdx -1] = "-d";
			command[refereeInputIdx] = "";
		}

		if (logs != null) {
			command[command.length - 2] = "-l";
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
					command[command.length - 1] = new StringBuilder(logs.toString()).append("/game").append(game)
							.append(".json").toString();
				}

				int seedRotate[] = SeedGenerator.getSeed(playersCount);
				if (swap) {
					command[refereeInputIdx] = "seed=" + seedRotate[0];
					for (int i = 0; i < playersCount; i ++) {
						command[pArgIdx[i]] = playersCmd.get((i + seedRotate[1]) % playersCount);
					}
				} else if (SeedGenerator.repeteableTests) {
					command[refereeInputIdx] = "seed=" + SeedGenerator.nextSeed();
				}

				referee = new BrutalProcess(Runtime.getRuntime().exec(command));

				boolean error = false;
				data.setLength(0);

				int[] scores = new int[playersCount];

				StringBuilder fullOut = new StringBuilder();
				try (Scanner in = referee.getIn()) {
					for (int pi = 0; pi < playersCount; ++pi) {
						int i = swap ? (pi + seedRotate[1]) % playersCount : pi;
						if (in.hasNextInt())
						{
							scores[i] = in.nextInt();
						}
						else
						{
							while(!in.hasNextInt() && in.hasNext())
							{
								fullOut.append(in.nextLine()).append("\n");
							}

							// Try again after referee messages are out of the way
							if (in.hasNextInt())
							{
								scores[i] = in.nextInt();
							}
						}

						if (scores[i] < 0) {
							error = true;
							LOG.error("Negative score during game " + game + " p" + i + ":" + scores[i]);
						}
					}

					while (in.hasNextLine()) {
						data.append(in.nextLine()).append("\n");
					}
				}

				if (fullOut.length()>0)
				{
					LOG.error("Problem with referee output in game" + game + ". Output content:" + fullOut);
				}

				if (checkForError()) {
					error = true;
				}

				if (error) {
					logHelp();
				}

				stats.add(scores);

				LOG.info(new StringBuilder().append("End of game ").append(game).append("\t").append(stats));

			} catch (Exception exception) {
				LOG.error("Exception in game " + game, exception);
				logHelp();
			} finally {
				destroyAll();
			}
		}
	}

	private void logHelp() {
		LOG.error("If you want to replay and see this game, use the following command line:");

		if (data.length() > 0) {
			LOG.error(String.join(" ", command) + " -s -d " + data);
		} else {
			LOG.error(String.join(" ", command) + " -s");
		}

	}

	private boolean checkForError() throws IOException {
		boolean error = false;

		try (BufferedReader in = referee.getError()) {
			if (in.ready()) {
				error = true;
				LOG.error("Error during game " + game);

				StringBuilder sb = new StringBuilder();
				while (in.ready()) {
					sb.append(in.readLine()).append("\n");
				}

				LOG.error(sb);
			}
		}

		return error;
	}

	private void destroyAll() {
		try {
			if (referee != null) {
				referee.destroy();
			}
		} catch (Exception exception) {
			LOG.error("Unable to destroy all");
		}
	}
}
