import java.util.ArrayList;
import java.util.List;

public class AIThread implements Runnable {
	
	AI player;
	playerAI pal;
	Board b;
	
	//Constructor allows easy access to player AI
	public AIThread(AI player) {
		this.player = player;
		this.b = player.b;
		this.pal = player.pal;
	}
	
	//Function automatically runs when thread starts
	@Override
	public void run() {
		System.out.println("Worker begin");
		playerAI helperAI = new playerAI();
		
		try {
			//Begin with an easy AI and work your way up to a very difficult AI
			for (int i = 1; i <= player.AIDifficulty; ++i) {
				//Only when a minimax can finish will 'pal' be updated with a best and worst move
				helperAI.minimax(b, i, 100, -100);
				pal.setBestMove(helperAI.returnBestMove());
				pal.setWorstMove(helperAI.returnWorstMove());
			}
		}
		catch(Exception e) {
			//Signal interrupts when timeout
	         System.out.println("Worker interrupted @ " + System.currentTimeMillis());
	         e.printStackTrace();
	         return;
	    } 
		System.out.println("Worker exit");
	}
	
}
