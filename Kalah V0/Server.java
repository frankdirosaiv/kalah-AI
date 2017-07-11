import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private ServerSocket listener;
	private boolean twoPlayers;
	private Application application;
	private Board board;

	public Connection player1;
	public Connection player2;

	// Temporary main statement until integrated!
	//public static void main(String[] args) {
	//	int[] pits = {4,4,4,4,4,4,4,4};
	//	Server server = new Server(1111, false, new Board(pits));
	//}

	Server(int port, boolean twoPlayers, Application application, boolean firstPlayer) {
		try {
			this.application = application;
			board = application.b;
			listener = new ServerSocket(port);
			if (twoPlayers) {
				this.twoPlayers = twoPlayers;
				player2 = new Connection(false);
			}
			else {
				player1 = new Connection(firstPlayer);
			}
		} catch (Exception e) {
			System.out.println("Failed to create server");
			closeConnection(); // Error establishing connection(s)
		}
	}

	public class Connection {
		private Socket socket;
		private BufferedReader input;
		private PrintWriter output;
		private boolean isPlayer1 = true;
		public boolean ready = false;

		Connection(boolean isPlayer1) {
			while (true) {
				try {
					socket = listener.accept();
					output = new PrintWriter(socket.getOutputStream(), true);
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					this.isPlayer1 = isPlayer1;
					sendMessage("WELCOME"); // Send welcome message
					int holes = (board.boardSize - 2)/2;
					int seeds = board.seedsPerPit;
					int time = application.MAXTIME * 1000;
					char order = isPlayer1 ? 'F' : 'S'; // Player 1 goes first
					char layout = board.boardType;
					int[] holeconfig = board.board;
					String infomsg = "INFO " + holes + " " + seeds + " " + time + " " + order + " " + layout;
					if (layout == 'R') {
						for (int i = 0; i < holeconfig.length/2; ++i) {
							infomsg = infomsg + " " + holeconfig[i];
						}
					}
					sendMessage(infomsg); // Send game config
					break;
				} catch (Exception e) {
					closeConnection();
				}
			}
		}
		
		public void parseMessage() throws IOException {
			String message = receiveMessage();
			System.out.println("Message: '" + message + "' from " + (isPlayer1 ? "Player One" : "Player Two"));
			if (message.startsWith("OK")) { // The client received the move
				if ((isPlayer1 && board.playerMove != 1) || (!isPlayer1 && board.playerMove != 2)) {
					return;
				}
				application.timeLeft = application.MAXTIME;
				// Wait for client to send a move
			}
			else if (message.startsWith("READY")) { // The client is ready
				ready = true;
				checkReady();
			}
			else if (message.startsWith("P")) { // Pie move
				sendMessage("OK");
				application.performMove(-1);
			}
			else { // Parse moves
				try {
					sendMessage("OK"); // Completed moves

					if ((isPlayer1 && board.playerMove != 1) || (!isPlayer1 && board.playerMove != 2)) {
						sendMessage("ILLEGAL");
						sendMessage("LOSER");
						if (twoPlayers) {
							if (isPlayer1) {
								sendToPlayer(2, "WINNER");
							}
							else {
								sendToPlayer(1, "WINNER");
							}
						}
						else {
							// The server won
						}
						return;
					}

					String[] moveMessage = message.split(" ");
					int[] moves = new int[moveMessage.length];
					for (int i = 0; i < moveMessage.length; ++i) {
						int move = Integer.parseInt(moveMessage[i]);
						if (move != -1) {
							moves[i] = move - 1;
							if (!isPlayer1) {
								moves[i] = moves[i] + application.b.p1KalahIndex + 1;
							}
						}
						else {
							moves[i] = -1;
						}
					}
					
					for (int i = 0; i < moves.length; ++i) {
						if (isPlayer1) {
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
				} catch (Exception e) {
					//sendMessage("ILLEGAL");
				}
			}
		}

		public boolean isPlayerOne() {
			return isPlayer1;
		}

		public String receiveMessage() throws IOException {
			return input.readLine();
		}

		public void sendMessage(String msg) throws IOException {
			output.println(msg);
		}

		public void closeConnection() {
			try {
				socket.close();
			} catch (Exception e) {}
		}
	}

	public void sendToPlayer(int player, String msg) {
		try {
			if (player == 1) {
				if (player1.isPlayerOne()) {
					System.out.println("Sending message to player " + player + " '" + msg + "'");
					try { player1.sendMessage(msg); } catch (Exception e) {}
				}
				else if (player2.isPlayerOne()) {
					System.out.println("Sending message to player " + player + " '" + msg + "'");
					try { player2.sendMessage(msg); } catch (Exception e) {}
				}
			}
			else if (player == 2) {
				if (!player1.isPlayerOne()) {
					System.out.println("Sending message to player " + player + " '" + msg + "'");
					try { player1.sendMessage(msg); } catch (Exception e) {}
				}
				else if (!player2.isPlayerOne()) {
					System.out.println("Sending message to player " + player + " '" + msg + "'");
					try { player2.sendMessage(msg); } catch (Exception e) {}
				}
			}
		} catch (Exception e) {}
	}
	
	public void checkReady() throws IOException {
		if (twoPlayers) {
			if (player1.ready && player2.ready) {
				sendToPlayer(1, "BEGIN");
				sendToPlayer(2, "BEGIN");
				application.timeLeft = application.MAXTIME;				
			}
		}
		else {
			if (player1.ready) {
				player1.sendMessage("BEGIN");
				application.timeLeft = application.MAXTIME;	
			}
		}
	}
	
	public boolean isTwoPlayers() {
		return twoPlayers;
	}

	public void listen() {
		try {
			player1.parseMessage();
		} catch (Exception e) {}
		try {
			player2.parseMessage();
		} catch (Exception e) {}
	}

	public void closeConnection() {
		try { player1.closeConnection(); } catch (Exception e) {}
		try { player2.closeConnection(); } catch (Exception e) {}
		try { listener.close(); } catch (Exception e) {}
	}
}