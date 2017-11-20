# cg-brutaltester

cg-brutaltester is a Java local tool to simulate the [CodinGame](https://www.codingame.com/) multiplayer IDE. How does it work? Very simple:

    java -jar cg-brutaltester.jar -r "java -jar cg-referee-ghost-in-the-cell.jar" -p1 "./myCode.exe" -p2 "php myCode.php" -t 2 -n 100 -l "./logs/"

At the end of the command, you will get something like this:

    13:19:47,629 INFO  [com.magusgeek.brutaltester.Main] *** End of games ***
    +----------+----------+----------+
    | Results  | Player 1 | Player 2 |
    +----------+----------+----------+
    | Player 1 |          | 7,00%    |
    +----------+----------+----------+
    | Player 2 | 52,70%   |          |
    +----------+----------+----------+

How to read it: Player 1 won 7.00% of the time against Player 2. Player 2 won 52.70% of the time against Player 1. The total is not 100% because you have some draws.

## Command line arguments:

### Referee `-r <string>` (Mandatory)

This is the command line to start the referee process. The referee must respect the cg-brutaltester protocol. See [How do I make my own referee?](#how-do-i-make-my-own-referee) for more information.
In our example, we use a runnable Jar file as the referee.

### Player X `-pX <string>` (Mandatory)

Each `-pX` argument is the command line to start a player process. You can give a maximum of 4 players. But don't forget the some referees will ignore some players (for example, Ghost in the Cell only uses 2 players).
In the example, the first player is a simple executable file and the second player is a php file.

### Threads `-t <int>` (Optional; Default is 1)

The number of threads to spawn for the games. If you give 2, it means that you will have 2 games playing at the same time. It's useless to spawn too many threads. If you have a 4-core CPU, you should not try to spawn more than 3 threads.

### Number of games `-n <int>` (Optional; Default is 1)

The number of games to play. The given example will play 100 games.

### Logs directory `-l <string>` (Optional)

You may need the logs of the file. If you specify a directory, all games will be saved in the given directory. The files contain standard and error outputs of all processes (referee and players).

### Swap player positions `-s`

There are some games (such as Tron), where one player has a disadvantage from the beginning on because of an asymmetric map. In this case you can repeat the game on the same map, but with positions changed. For more than two players this will perform a simple rotation and not test all permutations (resulting in 4 matches on the same map for 4 players instead of 24).
NOTE: not all referees support this flag, as they have to allow setting a seed.

### Verbose `-v`

Activate the verbose mode. Spam incoming.

### Help `-h`

Display this help :

    usage: -r <referee command line> -p1 <player1 command line> -p2 <player2 command line> -p3 <player3 command line> -p4 <player4 command line> [-v -n <games> -t <thread>]
     -h          Print the help
     -l <arg>    A directory for games logs
     -n <arg>    Number of games to play. Default 1.
     -p1 <arg>   Required. Player 1 command line.
     -p2 <arg>   Required. Player 2 command line.
     -p3 <arg>   Player 3 command line.
     -p4 <arg>   Player 4 command line.
     -r <arg>    Required. Referee command line.
     -s          swap player positions
     -t <arg>    Number of thread to spawn for the games. Default 1.
     -v          Verbose mode. Spam incoming.

## How do I make my own referee?

Your referee must be runnable with a command line (or you won't be able to give it to cg-brutaltester) and you have to use the standard input and output streams. The referee can output on the error stream for debug purposes or real errors. It will be stored in the log file of the game. cg-brutaltester is a very naive arena, and the referee must tell it how to work.

### Setting a seed (optional)

If you want to swap player positions (useful on asymmetric maps), read the line `###Seed N`, with N >= 0. This line will be passed even before `###Start`, if the `-s` flag is set for starting the brutaltester.
When getting the same seed, your referee has to create the same map.

### Start of a game

The first line received by the referee will be `###Start N`. Where `N` is the number of players for this game.

### Send inputs for a player

If the referee wants to give inputs for a player, it must first output `###Input X\n` where `X` is the index of the player (player 1 is `0`). Don't forget the `\n`. After this line, every output of the referee will be forwarded to the process of the player `X`.

### Asking for outputs of a player

If the referee wants to receive outputs from a player, it must output `###Output X N\n`. `X` is the index of the player (player 1 is `0`). `N` is the number of lines. Don't forget the `\n`. If you want 5 lines of outputs from player 3, just output `###Output 2 5\n`.

### End of the game

To stop the game, the referee must output `###End <results>\n`. Don't forget the `\n`. The `results` contain the position of each player separated by a space. If some players end at the same position, just put those players' indexes in the same position. Some examples:

 * `###End 0 2 3 1` : Player 1 is the winner. Player 3 is the second. Player 4 is the third. Player 2 is the fourth.
 * `###End 1 0` : Player 2 is the winner. Player 1 is the second.
 * `###End 01` : It's a draw between players 1 and 2.
 * `###End 1 03 2` : Player 2 is the winner. Player 1 and 4 are both at the second place. Player 3 is the third.
 * `###End 02 31` : Players 1 and 3 are both at the first place. Players 4 and 2 are both at the second place.

## Incoming features

This is not an official roadmap at all.

 * Generate an html file for the results (with graphics!)
 * Better handling of crashing players' code
 * Handle timeouts
 * Conquer the world

# List of compatible referees

If you have a bug or a problem with one of these referees, create an issue of the github project of the referee, not on cg-brutaltester project. This may not be a full list of available referees for cg-brutaltester. If you want to add a referee to this list, just make a pull request.

 * Ghost in the cell:
   * https://github.com/dreignier/cg-referee-ghost-in-the-cell (Java)
 * Coders of the Caribbean:
   * https://github.com/Coac/brutaltester-referee-coders-of-the-caribbean (Java)
   * https://github.com/KevinBusse/cg-referee-coders-of-the-caribbean (Java)
 * Code 4 life:
   * https://github.com/KevinBusse/cg-referee-code4life (Java)
 * Wondev Woman:
   * https://github.com/KevinBusse/cg-referee-wondev-woman (Java)
 * Mean Max:
   * https://github.com/KevinBusse/cg-referee-mean-max (Java)
 * Coders strike back:
   * https://github.com/robostac/coders-strike-back-referee (Go)
 * Back to the Code:
   * https://github.com/eulerscheZahl/RefereeCollection (C#)
 * Codebusters:
   * https://github.com/eulerscheZahl/RefereeCollection (C#)
 * Game of Drones:
   * https://github.com/eulerscheZahl/RefereeCollection (C#)
 * Hypersonic:
   * https://github.com/eulerscheZahl/RefereeCollection (C#)
 * Smash the Code:
   * https://github.com/eulerscheZahl/RefereeCollection (C#)
 * The Great Escape:
   * https://github.com/eulerscheZahl/RefereeCollection (C#)
 * Tron:
   * https://github.com/eulerscheZahl/RefereeCollection (C#)
