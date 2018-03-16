package com.magusgeek.brutaltester;

public class PlayerStats {
	private static final int VICTORY = 0;
	private static final int DEFEAT = 1;
	private static final int DRAW = 2;

	private int[][][] stats;
	private int[][] global;
	private int n;
	private int total;

	public PlayerStats(int n) {
		this.n = n;
		total = 0;
		stats = new int[n][n][3];
		global = new int[n][3];
	}

	synchronized public void add(int[] scores) {
		for (int i = 0; i < n; ++i) {
			for (int j = i + 1; j < n; ++j) {
				if (scores[i] > scores[j]) {
					stats[i][j][VICTORY] += 1;
					stats[j][i][DEFEAT] += 1;
					global[i][VICTORY] += 1;
					global[j][DEFEAT] += 1;
				} else if (scores[i] < scores[j]) {
					stats[j][i][VICTORY] += 1;
					stats[i][j][DEFEAT] += 1;
					global[j][VICTORY] += 1;
					global[i][DEFEAT] += 1;
				} else {
					stats[i][j][DRAW] += 1;
					stats[j][i][DRAW] += 1;
					global[i][DRAW] += 1;
					global[j][DRAW] += 1;
				}
			}
		}

		total += 1;
	}

	synchronized public void add(String line) {
		String[] params = line.split(" ");

		int[] positions = new int[n];

		for (int i = 1; i < params.length; ++i) {
			for (char c : params[i].toCharArray()) {
				positions[Character.getNumericValue(c)] = i - 1;
			}
		}

		for (int i = 0; i < n; ++i) {
			for (int j = i + 1; j < n; ++j) {
				if (positions[i] < positions[j]) {
					stats[i][j][VICTORY] += 1;
					stats[j][i][DEFEAT] += 1;
					global[i][VICTORY] += 1;
					global[j][DEFEAT] += 1;
				} else if (positions[i] > positions[j]) {
					stats[j][i][VICTORY] += 1;
					stats[i][j][DEFEAT] += 1;
					global[j][VICTORY] += 1;
					global[i][DEFEAT] += 1;
				} else {
					stats[i][j][DRAW] += 1;
					stats[j][i][DRAW] += 1;
					global[i][DRAW] += 1;
					global[j][DRAW] += 1;
				}
			}
		}

		total += 1;
	}

	private String percent(float amount) {
		return String.format("%.2f", amount * 100.0 / total) + "%";
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < n; ++i) {
			sb.append(" ").append(percent(global[i][VICTORY] / ((float) n - 1)));
		}

		return sb.toString();
	}

	synchronized public void print() {
		/*
        +----------+----------+----------+----------+----------+
        | Results  | Player 1 | Player 2 | Player 3 | Player 4 |
        +----------+----------+----------+----------+----------+
        | Player 1 |          |  42%     | 100%     |  32.46%  |
        +----------+----------+----------+----------+----------+
        | Player 2 | 42.36%   |          | 100%     |  100%    |
        +----------+----------+----------+----------+----------+
        | Player 3 | 42.36%   |  99.99%  |          |  100%    |
        +----------+----------+----------+----------+----------+
        | Player 4 | 42.36%   |  99.99%  | 17.8%    |          |
        +----------+----------+----------+----------+----------+
        */
		
		String separator = "";

		if (n == 2) {
			separator = "+----------+----------+----------+";
		} else if (n == 3) {
			separator = "+----------+----------+----------+----------+";
		} else if (n == 4) {
			separator = "+----------+----------+----------+----------+----------+";
		}

		System.out.println(separator);
		System.out.print("| Results  |");

		for (int i = 0; i < n; ++i) {
			System.out.print(" Player " + (i + 1) + " |");
		}
		System.out.println();
		System.out.println(separator);

		for (int i = 0; i < n; ++i) {
			System.out.print("| Player " + (i + 1) + " |");

			for (int j = 0; j < n; ++j) {
				String result = "";

				if (i != j) {
					result = percent(stats[i][j][VICTORY]);
				}

				System.out.print(" " + result + "         ".substring(result.length()) + "|");
			}

			System.out.println();
			System.out.println(separator);
		}
	}
}
