import java.util.*;

class Board {
	public int[] board = new int[20];
	public int p1KalahIndex = 6;
	public int p2KalahIndex = 13;
	public boolean allPitsEmpty = false;
	public int lastMove = 0;
	public int lastPlayerMove = 1;
	public int playerMove = 1;
	public int pitsPerPlayer = 6;
	public int boardSize = 14;
	public int numMoves = 0;
	public int seedsPerPit = 0;
	public char boardType = 'S';
	public int moveQueueLength = 0;
	public int[] moveQueue = new int[20];
	public boolean pieMovePossible = true;

	public Board(int [] array) {

		boardSize = array.length;
		p1KalahIndex = (array.length - 2) / 2;
		p2KalahIndex = array.length - 1;
		pitsPerPlayer = (array.length - 2) / 2;

		for (int i = 0; i < array.length; i++) {
			board[i] = array[i];
		}
	}

	Board(Board copy) {
		pieMovePossible = copy.pieMovePossible;
		seedsPerPit = copy.seedsPerPit;
		boardType = copy.boardType;
		pitsPerPlayer = copy.pitsPerPlayer;
		numMoves = copy.numMoves;
		boardSize = copy.boardSize;
		p1KalahIndex = copy.p1KalahIndex;
		p2KalahIndex = copy.p2KalahIndex;
		allPitsEmpty = copy.allPitsEmpty;
		int index = 0;
		for (int i = 0; i < copy.boardSize; i++) {
			board[index] = copy.board[i];
			index++;
		}
	}

	public int player1Score() {
		return board[p1KalahIndex];
	}

	public int player2Score() {
		return board[p2KalahIndex];
	}

	public void printBoard() {
		System.out.println("             Player 2");
		System.out.print("  ");
		for (int i = boardSize - 2; i > pitsPerPlayer; i--) {
			System.out.print("  " + board[i] + "  ");
		}
		System.out.print("\n");
		System.out.print(board[p2KalahIndex] + " ");
		for (int i = 0; i < pitsPerPlayer; i++) {
			System.out.print("     ");
		}
		System.out.print(board[p1KalahIndex] + "\n" + "  ");
		for (int i = 0; i < pitsPerPlayer; i++) {
			System.out.print("  " + board[i] + "  ");
		}
		System.out.print("\n" + "             Player 1" + "\n\n");
	}





	public int getUserInput (int player) {
		//Scan for user input
		Scanner reader = new Scanner(System.in);
		if (player == 1) {
			System.out.println("Enter your move Player 1:");
		}
		else {
			System.out.println("Enter your move Player 2:");
		}

		String input = reader.nextLine();
		int currentPit;
		if(Objects.equals(input, new String("P"))) {
			return -1;
		}
		else {
			currentPit = Integer.parseInt(input);
		}

		return currentPit;
	}

	//Return the player whos supposed to make the next move
	public int player1move(int currentPit) {

		if (currentPit == -1) {
			if (numMoves == 1 && pieMovePossible) {
				for(int i = 0; i < p1KalahIndex + 1; ++i) {
					int tempSwap = board[i];
					board[i] = board[p1KalahIndex + 1 + i];
					board[i + p1KalahIndex + 1] = tempSwap;
				}
				lastMove = -1;
				++numMoves;
				playerMove = 2;
				return 2;
			}
			else {
				System.out.println("Cannot perform Pie Move at this time- Try again...");
				playerMove = 1;
				return 1;
			}
		}


		//Begin player picking up seeds from pit
		//If pit is out range then send error
		if (currentPit > boardSize) {
			System.out.println("Pit chosen is out of range- Try again...");
			playerMove = 1;
			return 1;
		}

		//If pit is on opponents side, invalid move
		if (currentPit > p1KalahIndex) {
			System.out.println("Cannot pick a pit on your opponents side- Try again...");
			playerMove = 1;
			return 1;
		}

		//If pit is empty, invalid move
		if (board[currentPit] == 0) {
			System.out.println("Cannot pick an empty pit- Try again...");
			playerMove = 1;
			return 1;
		}

		//If pit is Kalah, invalid move
		if (currentPit == p1KalahIndex) {
			System.out.println("Cannot pick a Kalah as a pit- Try again...");
			playerMove = 1;
			return 1;
		}

		//Take seeds out of currentPit
		int seedsInHand = board[currentPit];
		board[currentPit] = 0;
		lastMove = currentPit;


		//Traverse board and dispense seeds
		while (seedsInHand > 0) {
			++currentPit;
			currentPit = currentPit % boardSize;

			//Do not place seed if currentPit is opponents Kalah
			if (currentPit != p2KalahIndex) {
				++board[currentPit];
				--seedsInHand;
			}

		}

		//If last seed landed in an empty pit, you capture the adjacent opponent seeds
		//If there are no adjacent opponent seeds, you play as normal
		//If you land on your opponents side, you play as normal
		if (board[currentPit] == 1 && currentPit < p1KalahIndex) {
			int adjacentPit = boardSize - currentPit - 2;
			if (board[adjacentPit] > 0) {
				board[p1KalahIndex] += board[currentPit];
				board[p1KalahIndex] += board[adjacentPit];
				board[currentPit] = 0;
				board[adjacentPit] = 0;

				//Check if the seed capture move emptied your opponents pits
				allPitsEmpty = true;
				for (int i = p1KalahIndex + 1; i < p2KalahIndex; ++i) {
					if (board[i] != 0) {
						allPitsEmpty = false;
						break;
					}
				}
				if (allPitsEmpty) {
					for (int i = 0; i < p1KalahIndex; ++i) {
						board[p1KalahIndex] += board[i];
						board[i] = 0;
					}
					++numMoves;
					playerMove = 2;
					return 2; //Questionable return value
				}
			}
		}

		//If at the end of your move, you have no seeds, your opponent puts the rest of their seeds in their Kalah
		//Idea to improve performance, only execute this when there is one seed next Kalah or just performed a swipe
		allPitsEmpty = true;
		for (int i = 0; i < p1KalahIndex; ++i) {
			if (board[i] != 0) {
				allPitsEmpty = false;
				break;
			}
		}
		if (allPitsEmpty) {
			for (int i = p1KalahIndex + 1; i < p2KalahIndex; ++i) {
				board[p2KalahIndex] += board[i];
				board[i] = 0;
			}
			++numMoves;
			playerMove = 2;
			return 2;
		}

		//If your last seed ended in your Kalah, it's your turn again
		if (currentPit == p1KalahIndex) {
			if (numMoves == 1) {
				pieMovePossible = false;
			}
			playerMove = 1;
			return 1;
		}
		else {
			playerMove = 2;
			++numMoves;
			return 2;
		}
	}


	//Return the player who is supposed to make the next move
	public int player2move(int currentPit) {

		if (currentPit == -1) {
			if (numMoves == 1 && pieMovePossible) {
				for(int i = 0; i < p1KalahIndex + 1; ++i) {
					int tempSwap = board[i];
					board[i] = board[p1KalahIndex + 1 + i];
					board[i + p1KalahIndex + 1] = tempSwap;
				}
				lastMove = -1;
				++numMoves;
				playerMove = 1;
				return 1;
			}
			else {
				System.out.println("Cannot perform Pie Move at this time- Try again...");
				playerMove = 2;
				return 2;
			}
		}


		//Begin player picking up seeds from pit
		//If pit is out range then send error
		if (currentPit > boardSize) {
			System.out.println("Pit chosen is out of range- Try again...");
			playerMove = 2;
			return 2;
		}

		//If pit is on opponents side, invalid move
		if (currentPit <= p1KalahIndex) {
			System.out.println("currentPit: " + currentPit);
			System.out.println("Cannot pick a pit on your opponents side- Try again...");
			playerMove = 2;
			return 2;
		}

		//If pit is empty, invalid move
		if (board[currentPit] == 0) {
			System.out.println("Cannot pick an empty pit- Try again...");
			playerMove = 2;
			return 2;
		}

		//If pit is Kalah, invalid move
		if (currentPit == p2KalahIndex) {
			System.out.println("Cannot pick a Kalah as a pit- Try again...");
			playerMove = 2;
			return 2;
		}

		//Take seeds out of currentPit
		int seedsInHand = board[currentPit];
		board[currentPit] = 0;
		lastMove = currentPit;


		//Traverse board an dispense seeds
		while (seedsInHand > 0) {
			++currentPit;
			currentPit = currentPit % boardSize;

			//Do not place seed if currentPit is opponents Kalah
			if (currentPit != p1KalahIndex) {
				++board[currentPit];
				--seedsInHand;
			}

		}

		//If last seed landed in an empty pit, you capture the adjacent opponent seeds
		//If there are no adjacent opponent seeds, you play as normal
		//If you land on your opponents side, you play as normal
		if (board[currentPit] == 1 && currentPit < p2KalahIndex && currentPit > p1KalahIndex) {
			int adjacentPit = boardSize - currentPit - 2;
			if (board[adjacentPit] > 0) {
				board[p2KalahIndex] += board[currentPit];
				board[p2KalahIndex] += board[adjacentPit];
				board[currentPit] = 0;
				board[adjacentPit] = 0;

				//Check to see if the seed capture ended the game
				allPitsEmpty = true;
				for (int i = 0; i < p1KalahIndex; ++i) {
					if (board[i] != 0) {
						allPitsEmpty = false;
						break;
					}
				}
				if (allPitsEmpty) {
					for (int i = p1KalahIndex + 1; i < p2KalahIndex; ++i) {
						board[p2KalahIndex] += board[i];
						board[i] = 0;
					}
					++numMoves;
					playerMove = 1;
					return 1; //Questionable value
				}
			}
		}

		//If at the end of your move, you have no seeds, your opponent puts the rest of their seeds in their Kalah
		//Idea to improve performance, only execute this when there is one seed next Kalah or just performed a swipe
		allPitsEmpty = true;
		for (int i = p1KalahIndex + 1; i < p2KalahIndex; ++i) {
			if (board[i] != 0) {
				allPitsEmpty = false;
				break;
			}
		}
		if (allPitsEmpty) {
			for (int i = 0; i < p1KalahIndex; ++i) {
				board[p1KalahIndex] += board[i];
				board[i] = 0;
			}
			++numMoves;
			playerMove = 1;
			return 1;
		}

		//If your last seed ended in your Kalah, it's your turn again
		if (currentPit == p2KalahIndex) {
			if (numMoves == 1) {
				pieMovePossible = false;
			}
			playerMove = 2;
			return 2;
		}
		else {
			playerMove = 1;
			++numMoves;
			return 1;
		}
	}
}
