import java.util.LinkedList;
import java.util.Queue;

public class Human extends Player{
	
	//Human uses super constructor to hold data
	Human(Application app, Board b, int player) {
		super(app, b, player);
	}
	

	public String move(int index) {
		String moveMsg = "";
		//Insert move based on the user button clicked
		int moveindex = index + 1;
		if (player != 1) {
			moveindex = moveindex - b.p1KalahIndex - 1;
		}
		if (index != -1) {
			moveQueue.add(moveindex);
		}
		else {
			if (app.networking == 1) {
				if (player == 1) {
					app.SERVER.sendToPlayer(2, "P");
				}
				else {
					app.SERVER.sendToPlayer(1, "P");
				}
			}
			else if (app.networking == 2) {
				if (player != 1 && !app.CLIENT.isPlayerOne()) {
					try { app.CLIENT.sendMessage("P"); } catch (Exception e) {}
				}
			}
		}
		
		//based on player, send off message when a turn ends
		if(player == 1) {
			if (b.player1move(index) != 1) {
				moveMsg = queueToString(moveQueue);
				System.out.println("ALL MOVES: " + moveMsg);
				app.prevMove = "Previous move: House " + moveMsg;
				emptyQueue();
			}
		}
		else if (player == 2) {
			if (b.player2move(index) != 2) {
				moveMsg = queueToString(moveQueue);
				System.out.println("ALL MOVES: " + moveMsg);
				app.prevMove = "Previous move: House " + moveMsg;
				emptyQueue();
			}
		}
		
		//Update buttons after every button click by user
		app.updateButtons();
		return moveMsg;
	}
	
}
