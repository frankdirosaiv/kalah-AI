import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private boolean isAI = false;
	private boolean player1 = false;
	private boolean connected = true;
	private Socket server;
	private BufferedReader input;
	private PrintWriter output;
	private Application application;
	private Board board;
	private int time = 60;

	// Temporary main statement until integrated!
	//public static void main(String[] args) {
	//	Client client = new Client("127.0.0.1", 1111, false);
	//}

	Client(String ip, int port, boolean ai, Application application) {
		try {
			this.application = application;
			server = new Socket(ip, port);
			input = new BufferedReader(new InputStreamReader(server.getInputStream()));
			output = new PrintWriter(server.getOutputStream(), true);
			isAI = ai;
			startConnection();
		} catch (Exception e) {
			System.out.println("Failed to create client");
			closeConnection();
		}
	}

	public void startConnection() throws IOException {
		String message = input.readLine();
		if (message.startsWith("WELCOME")) { // Initialize
			System.out.println("Connected");
			String configmsg = input.readLine();
			String[] config = configmsg.split(" ");
			if (config.length >= 5 && config[0].startsWith("INFO")) {
				int holes = Integer.parseInt(config[1])*2 + 2;
				System.out.println("Holes: " + holes);
				int seeds = Integer.parseInt(config[2]);
				System.out.println("Seeds: " + seeds);
				time = Integer.parseInt(config[3])/1000;
				application.MAXTIME = time;
				System.out.println("Time: " + time);

				boolean first = config[4].startsWith("F");
				System.out.println("Player 1: " + first);
				
				if (first) {
					player1 = true;
				}
				else {
					player1 = false;
				}
				
				boolean random = config[4].startsWith("R");
				System.out.println("Random: " + random);
				int[] holeconfig = new int[holes];
				if (random) {
					holeconfig = new int[holes];
					for (int i = 0; i < holes; ++i) {
						holeconfig[i] = Integer.parseInt(config[i+6]);
					}
				}

				if (random) {
					board = new Board(holeconfig);
				}
				else {
					int[] pits = new int[holes];
					for (int i = 0; i < holes; ++i) {
						if (i == (holes-2)/2 || i == holes-1) {
							pits[i] = 0;
						} else {
							pits[i] = seeds;
						}
					}
					board = new Board(pits);
				}
				System.out.println("Client created board");
				
				application.b = board;
				if (!player1) {
					System.out.println("Client (Player 2) creating game");
					application.player1 = new Human(application, application.b, 1);
					if (isAI) {
						application.player2 = new AI(application, application.b, 2, 10);
					}
					else {
						application.player2 = new Human(application, application.b, 2);
					}
				}
				else {
					System.out.println("Client (Player 1) creating game");
					application.player2 = new Human(application, application.b, 2);
					if (isAI) {
						application.player1 = new AI(application, application.b, 1, 10);
					}
					else {
						application.player1 = new Human(application, application.b, 1);
					}
				}
				application.startClientListener();
				sendMessage("READY");
				System.out.println("Client finished creating game");
			}
			else {
				System.out.println("Received invalid configuration info");
				closeConnection(); // Was not initialized properly
			}
		}
		else {
			System.out.println("Did not receive 'WELCOME' message");
			closeConnection();
		}
	}

	public void listen() {
		try {
			String message = input.readLine();
			System.out.println("Message: '" + message + "' from Server");
			if (message.startsWith("OK")) { // The server received the move
				application.timeLeft = time;
			}
			else if (message.startsWith("ILLEGAL")) { // Illegal move
				// Let the client know that they tried an illegal move
			}
			else if (message.startsWith("TIME")) { // Time ran out
				// Let the client know that it ran out of time
			}
			else if (message.startsWith("WINNER")) { // The client won
				application.endGame();
			}
			else if (message.startsWith("LOSER")) { // The client lost
				application.endGame();
			}
			else if (message.startsWith("TIE")) { // The game ended in a tie
				application.endGame();
			}
			else if (message.startsWith("P")) { // Pie move
				sendMessage("OK");
				application.performMove(-1);
			}
			else if (message.startsWith("BEGIN")) { // Start the game
				application.game();
				application.timeLeft = application.MAXTIME;
			}
			else { // Parse moves
				try {
					sendMessage("OK"); // Completed moves
					application.timeLeft = time;

					String[] moveMessage = message.split(" ");
					int[] moves = new int[moveMessage.length];
					for (int i = 0; i < moveMessage.length; ++i) {
						int move = Integer.parseInt(moveMessage[i]);
						if (move != -1) {
							moves[i] = move - 1;
							if (player1) {
								moves[i] = moves[i] + application.b.p1KalahIndex + 1;
							}
						}
						else {
							moves[i] = -1;
						}
					}
					
					for (int i = 0; i < moves.length; ++i) {
						if (!player1) {
							application.performMove(moves[i]);
							if (board.playerMove == 1) {
								continue;
							}
							else {
								break;
							}
						}
						else {
							application.performMove(moves[i]);
							if (board.playerMove == 2) {
								continue;
							}
							else {
								break;
							}
						}
					}

					//if (player1) {
					//	application.player1.enable();
					//}
					//else {
					//	application.player2.enable();
					//}
				} catch (Exception e) {
					// We couldn't parse the moves
				}
			}
		} catch (Exception e) {}
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean isPlayerOne() {
		return player1;
	}

	public void closeConnection() {
		//sendMessage("EXIT"); // Possibly let the server know that the connection is being terminated
		connected = false;
		try { server.close(); } catch (Exception e) {}
	}

	public void sendMessage(String msg) throws IOException {
		output.println(msg);
		System.out.println("SENDING MESSAGE: " + msg);
	}
}