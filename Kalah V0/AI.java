import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class AI extends Player{
	playerAI pal;
	int AIDifficulty;

	//Using polymorphism create an AI player
	AI(Application app, Board b, int player, int AIDifficulty) {
		super(app, b, player);
		this.pal = new playerAI();
		this.AIDifficulty = AIDifficulty;
	}
	
	//Select any valid move to trigger the application to perform a move
	//Note that '0' and 'p1KalahIndex + 1' are simply valid moves, not the actual moves the AI is choosing
	public void enable() {
		if (player == 1) {
			app.performMove(0);
		}
		else if (player == 2) {
			app.performMove(b.p1KalahIndex + 1);
		}
	}
	
	//Return a all moves that the player has made in his turn
	public String move(int index) {
		String moveMsg = "";		

		//Begin a thread to determine an AIs best move
		if (player == 1) {
			AIThread r = new AIThread(this);
			Thread t = new Thread(r);
			
			//Iterative deepening searches AI until a max time is reached
			t.start();
			long time_limit = app.timeLeft * 1000 / 2;
		    System.out.println("Time limit = " + time_limit);
		    
		    try {
				Thread.sleep(time_limit);
				app.timeLeft = app.timeLeft - (int)(time_limit/1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    
		    //Interrupt the thread when time has run out
		    t.interrupt();	      
		      
		    index = pal.returnWorstMove();
		    if (index != -1) {
				moveQueue.add(index + 1);
			}
			else {
				if (app.networking == 1) {
					app.SERVER.sendToPlayer(2, "P");
				}
				else if (app.networking == 2) {
					try { app.CLIENT.sendMessage("P"); } catch (Exception e) {}
				}
			}
			//If turn is over, then return all the moves a player has made in his turn
			if (b.player1move(pal.returnWorstMove()) != 1) {
				moveMsg = queueToString(moveQueue);
				System.out.println("ALL MOVES: " + moveMsg);
				app.prevMove = "Previous move: House " + moveMsg;
				emptyQueue();
			}
		}
		else if (player == 2) {
			
			AIThread r = new AIThread(this);
			Thread t = new Thread(r);
			
			//Iterative deepening searches AI until a max time is reached
			t.start();
			long time_limit = app.timeLeft * 1000 / 2;
		    System.out.println("Time limit = " + time_limit);
		    
		    try {
				Thread.sleep(time_limit);
				app.timeLeft = app.timeLeft - (int)(time_limit/1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    
		    //Interrupt the thread when time has run out
		    t.interrupt();	

		    index = pal.returnBestMove();
		    if (index != -1) {
				moveQueue.add(index - b.p1KalahIndex);
			}
			else {
				if (app.networking == 1) {
					app.SERVER.sendToPlayer(1, "P");
				}
				else if (app.networking == 2) {
					try { app.CLIENT.sendMessage("P"); } catch (Exception e) {}
				}
			}
			//If turn is over, then return all the moves a player has made in his turn
			if (b.player2move(pal.returnBestMove()) != 2) {
				moveMsg = queueToString(moveQueue);
				System.out.println("ALL MOVES: " + moveMsg);
				app.prevMove = "Previous move: House " + moveMsg;
				emptyQueue();
			}
		}
		
		System.out.println("Player Moves "  + moveMsg);
		
		//Update buttons in gui
		if (b.numMoves > 1){
			app.pieButton.doClick();
		}
		app.updateButtons();
		app.p1Houses.setVisible(true);
		return moveMsg;
	}
	
}