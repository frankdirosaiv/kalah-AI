import java.util.LinkedList;
import java.util.Queue;

public class Player {
	//Hold the board in application
	public Board b;
	//Hold the application
	public Application app;
	//Hold who's turn it is
	int player;
	//Hold all moves player makes in his turn
	Queue<Integer> moveQueue = new LinkedList<>();
	
	//Empty constructor goes unused, but is needed for compilation
	Player() {
	}
	
	//Super constructor
	Player(Application app, Board b, int player) {
		this.app = app;
		this.b = b;
		this.player = player;
	}
	
	//Return the score of the designated player
	protected final int score() {
		if(player == 1) {
			return b.board[b.p1KalahIndex];
		}
		else if (player == 2) {
			return b.board[b.p2KalahIndex];
		}
		else {
			System.out.println("Player must be player 1 or 2");
			return -1;
		}
	}
	
	//Convert a queue of moves to a string
	public String queueToString(Queue<Integer> moveQueue) {
		String moveMsg = "";
		if (!moveQueue.isEmpty()) {
			moveMsg = moveMsg + moveQueue.peek();
			moveQueue.remove();
			for (int i = 0; i < moveQueue.size(); ++i) {
				moveMsg = moveMsg + " " + moveQueue.peek();
				moveQueue.remove();
			}
		}
		return moveMsg;
	}
	
	//virtual function used by AI
	public void enable() {
	}
	
	//Once a turn has ended, the queue will be emptied
	public void emptyQueue() {
		for (int i = 0; i < moveQueue.size(); ++i) {
			moveQueue.remove();
		}
	}
	
	
	public String move(int index) {
		return "";
	}
	
}
