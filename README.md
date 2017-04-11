# cg-brutaltester

cg-brutaltester is a Java local tool to simulate the [CodinGame](https://www.codingame.com/) multiplayer IDE. How it works ? Very simple :

    java -jar cg-brutaltester.jar -r "java -jar cg-referee-ghost-in-the-cell.jar" -p1 "./myCode.exe" -p2 "php myCode.php" -t 2 -n 100 -l "./logs/"
    
## Command line arguments:

### Referee `-r <string>` Mandatory.

This is the command line to start the referee process. The referee must respect the cg-brutaltester protocol. See "How to make my own referee ?" for more informations.
In our example, we use a runnable Jar file as the referee.

### Player X `-pX <string>` Mandatory.

Each `-pX` argument is the command line to start a player process. You can give a maximum of 4 players. But don't forget the some referee will ignore some player (for example, Ghost in the Cell only use 2 players).
In the example, the first player is a simple executable file and the second player is a php file.

### Threads `-t <int>` Default is 1.

The number of thread to spawn for the games. If you give 2, it means that you will have 2 games playing at the same time. It's useless to spawn too many thread. If you have a 4 cores CPU, you should not try to spawn more than 3 threads.

### Number of games `-n <int>` Default is 1.

The number of games to play. The given example will play 100 games.

### Logs directory `-l <string>`

You may need the logs of the file. If you specify a directory, all games will be saved in the given directory. The files contains standard and errors outputs of all process (referee and players).

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
     -t <arg>    Number of thread to spawn for the games. Default 1.
     -v          Verbose mode. Spam incoming.

## How to make my own referee ?

Your referee must be runnable with a command line (or you won't be able to give it to cg-brutaltester) and you have to use the standard input and output streams. The referee can output on the error stream for debug purposes or real errors. It will be stored in the log file of the game. cg-brutaltester is a very naive arena, and the referee must tell it how to work.

### Start of a game

The first line received by the referee will be `###Start N`. Where `N` is the number of players for this game.

### Send intputs for a player

If the referee want to give inputs for a player, it must first output `###Input X\n` where `X` is the index of the player (player 1 is `0`). Don't forge the `\n`. After this line, every output of the referee will be forwarded to the process of the player `X`.

### Asking for outputs of a player

If the referee want to receive outputs from a player, it must output `###Output X N`. `X` is the index of the player (player 1 is `0`). `N` is the number of lines. If you want 5 lines of outputs from player 3, just output `###Output 2 5`.

### End of the game

To stop the game, the referee must output `###End <results>`. The `results` contains the position of each players separated by a space. If some players at the same position, just put the players index in the same position. Some examples:

 * `###End 0 2 3 1` : Player 1 is the winner. Player 3 is the second. Player 4 is the third. Player 2 is the fourth.
 * `###End 1 0` : Player 2 is the winner. Player 1 is the second.
 * `###End 01` : It's a draw between players 1 and 2.
 * `###End 1 03 2` : Player 2 is the winner. Player 1 and 4 are both at the second place. Player 3 is the third.
 * `###End 02 31` : Players 1 and 3 are both at the first place. Players 4 and 2 are both at the second place.
 
## Incoming features

This is not an official roadmap at all.

 * Generate an html file for the results (with graphics !)
 * Better handling of crashing players code
 * Handle timeouts
 * Conquer the world 
 
# List of compatibles referees

 * Ghost in the cell (Java) : https://github.com/dreignier/cg-referee-ghost-in-the-cell



