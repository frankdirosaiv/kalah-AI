import java.util.*;

public class playerAI {

	//Arbitrary move that doesnt exist
	private int bestMove = -20;
	private int worstMove = -20;

	public int returnBestMove() {
		return bestMove;
	}

	public void setBestMove(int move) {
		bestMove = move;
	}

	public int returnWorstMove() {
		return worstMove;
	}

	public void setWorstMove(int move) {
		worstMove = move;
	}

	//Determine the state of the board
	public int utilityFunction(Board b) {
		if (b.allPitsEmpty) {
			if (b.player2Score() > b.player1Score()) {
				return 100; //Player 2 has won
			}
			else {
				return -100; //Player 1 has won
			}
		}
		return b.player2Score() - b.player1Score();
	}


	//Determine all possible child states
	public List<Board> getAvailableStates(Board b) {
		List<Board> childStates = new ArrayList<Board>();

		//Make every possible move that player 1 could make
		if (b.playerMove == 1) {
			for (int i = 0; i < b.p1KalahIndex; ++i) {
				if (b.board[i] != 0) {
					Board possibleMove = new Board(b);

					possibleMove.player1move(i);

					childStates.add(possibleMove);
					//possibleMove.printBoard();
				}
			}
			if (b.numMoves == 1 && b.pieMovePossible) {
				Board possibleMove = new Board(b);

				possibleMove.player2move(-1);

				childStates.add(possibleMove);
				//possibleMove.printBoard();
			}
		}

		//Make every possible move that player 2 could make
		else if (b.playerMove == 2) {
			for (int i = b.p1KalahIndex + 1; i < b.p2KalahIndex; ++i) {
				if (b.board[i] != 0) {
					Board possibleMove = new Board(b);

					possibleMove.player2move(i);

					childStates.add(possibleMove);
					//possibleMove.printBoard();
				}
			}
			if (b.numMoves == 1 && b.pieMovePossible) {
				Board possibleMove = new Board(b);

				possibleMove.player2move(-1);

				childStates.add(possibleMove);
				//possibleMove.printBoard();
			}
		}
		else {
			System.out.println("Player does not exist");
		}
		return childStates;
	}

	//Return the highest possible score that can be achieved
	public int minimax(Board b, int depth, int alpha, int beta) {
		int bestLocalUtility = 0;
		int bestLocalMove = 0;
		int worstLocalMove = 0;


		//Arbitrarily horrible moves to be replaced by anything
		if (b.playerMove == 1) {
			bestLocalUtility = 100;
		}
		else if (b.playerMove == 2) {
			bestLocalUtility = -100;
		}

		//We have not yet reached the deepest level of our minimax tree, so we must expand
		if (depth > 0 && !b.allPitsEmpty) {
			List<Board> childStates = getAvailableStates(b);

			//Inspect each possible child board state
			for (Board child : childStates) {
				int childUtility;

				//Update pruning variables
				if(b.playerMove == 1) {
					alpha = bestLocalUtility; //player 1 will not choose a greater move than alpha
				}
				else if (b.playerMove == 2) {
					beta = bestLocalUtility; //player 2 will not choose a lesser move than beta
				}

				//Expand tree on child state
				if (child.playerMove == b.playerMove) {
					//Expand tree when it is still the same players move
					childUtility = minimax(child, depth, alpha, beta);
				}
				else {
					//Expand tree when it is the next player's move
					childUtility = minimax(child, depth - 1, alpha, beta);
				}

				//Update best Local Score and save the move that created this score
				if (b.playerMove == 2) {
					if (childUtility >= bestLocalUtility) {
						bestLocalUtility = childUtility;
						bestLocalMove = child.lastMove;
						if (bestLocalUtility > alpha) {
							//System.out.println("Pruned " + alpha + " because best Util is " + bestLocalUtility);
							break;
						}
					}
				}
				//Update the best local Score
				else if (b.playerMove == 1) {
					if (childUtility <= bestLocalUtility) {
						bestLocalUtility = childUtility;
						worstLocalMove = child.lastMove;
						if (bestLocalUtility < beta) {
							//System.out.println("Pruned " + beta + " because best Util is " + bestLocalUtility);
							break;
						}
					}
				}
			}

			worstMove = worstLocalMove;
			bestMove = bestLocalMove;
			return bestLocalUtility;
		}
		//Determine the score of the leaf node
		else {
			//b.printBoard();
			return utilityFunction(b);
		}

	}


}
