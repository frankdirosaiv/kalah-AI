import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class Application extends JFrame implements ActionListener {
	// global variables for GUI elements
	JFrame titleFrame;
	JPanel titleLabelPanel;
	JPanel titleButtonPanel;
	JLabel titleLabel;
	JLabel nameLabel;
	JButton titleSLButton;
	JButton titleClientButton;
	JFrame optionsFrame;
	JPanel selectPanel;
	JButton selectOptions;
	JButton selectServer;
	JButton selectClient;
	JButton pieButton;
	JLabel houseLabel;
	JLabel seedLabel;
	JLabel randomLabel;
	JLabel cpuLabel;
	JLabel p1Label;
	JLabel p2Label;
	JPanel optionsPanel;
	JComboBox houseBox;
	JComboBox seedBox;
	JComboBox randomBox;
	JComboBox cpuBox;
	JComboBox p1Box;
	JComboBox p2Box;
	ArrayList<JButton> p1buttons;
	ArrayList<JButton> p2buttons;
	JPanel p1StorePanel;
	JLabel p1Store;
	JPanel p2StorePanel;
	JLabel p2Store;
	JPanel p1Houses;
	JPanel p2Houses;
	JPanel timerPanel;
	JLabel timerLabel;
	JLabel winnerLabel;
	Board b;
	JFrame gameFrame;
	playerAI al;
	boolean gameOver;
	GridBagConstraints c = new GridBagConstraints();
	Font scoreFont = new Font("Helvetica", Font.BOLD, 50);
	Font moveFont = new Font("Helvetica", Font.BOLD, 30);
	int seeds;
	char boardState;
	String prevMove;
	JLabel prevMoveLabel;
	String gameType;

	ArrayList<JLabel> p1HouseLabels;
	ArrayList<JLabel> p2HouseLabels;

	// more global variables
	private static int[] globalArr;
	private static int difficulty;
	private static boolean pieMove;
	private static boolean optionsFinished = false;
	public int numberOfMoves = 0;
	public int MAXTIME = 60;
	public int timeLeft;
	public int networking = 0;
	public Server SERVER;
	public Client CLIENT;
	Player player1;
	Player player2;


	// this is called every time a button is pressed
	public void updateButtons() {
		int h = b.pitsPerPlayer;
		for (int i = 0; i < p1buttons.size(); i++) {
			p1buttons.get(i).setText(String.valueOf(b.board[i]));
		}
		for (int i = 0; i < p2buttons.size(); i++) {
			p2buttons.get(h-i-1).setText(String.valueOf(b.board[i + h + 1]));
		}
		p1Store.setText(String.valueOf(b.board[h]));
		p2Store.setText(String.valueOf(b.board[h*2+1]));
		prevMoveLabel.setText(prevMove);

		if (b.pieMovePossible && b.numMoves == 1) {
			pieButton = new JButton("House -1");
			pieButton.setText("Pie move");
			
			pieButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if (b.pieMovePossible && b.numMoves == 1) {
						performMove(-1);
					}
					System.out.println("delete pieButton");
					timerPanel.remove(pieButton);
					timerPanel.revalidate();
					timerPanel.repaint();
				}
			});
			c.gridy = 3;
			timerPanel.add(pieButton);
		}
		revalidate();
		System.out.println("revalidate called");

		if (b.allPitsEmpty) {
			gameOver = true;
			endGame();
		}
	}

	public void startServerListener() {
		java.util.Timer serverTimer = new java.util.Timer();
		serverTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				SERVER.listen();
			}
		}, 10, 10);
	}

	public void startClientListener() {
		java.util.Timer clientTimer = new java.util.Timer();
		clientTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				CLIENT.listen();
			}
		}, 10, 10);
	}

	// controls the timer
	public void startTimer() {
		javax.swing.Timer moveTimer = new javax.swing.Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeLeft--;
				timerLabel.setText(String.valueOf(timeLeft));
				revalidate();
				if (timeLeft == 0 && !gameOver) {
					timerExpired();
				}
			}
		});
		moveTimer.start();
	}

	// triggers a loss if the timer expires
	public void timerExpired() {
		c.insets = new Insets(10,10,10,10);
		p1Houses.setVisible(false);
		p2Houses.setVisible(false);
		timerPanel.removeAll();
		winnerLabel = new JLabel();
		winnerLabel.setFont(scoreFont);

		int loser = b.playerMove;
		winnerLabel.setText("PLAYER " + loser + " LOSES: TIME EXPIRED");
		timerPanel.add(winnerLabel, c);

		if (networking == 1) {
			SERVER.sendToPlayer(1, "TIME");
			SERVER.sendToPlayer(2, "TIME");
			SERVER.sendToPlayer(1, (loser == 1) ? "LOSER" : "WINNER");
			SERVER.sendToPlayer(2, (loser == 2) ? "LOSER" : "WINNER");
		}

		revalidate();
	}

	// normal end game scenario
	public void endGame() {
		JLabel highScoreLabel = new JLabel("NEW HIGH SCORE");
		File file = new File(gameType + ".txt");
		int highScore;
		int gameScore;

		c.insets = new Insets(10,10,10,10);
		p1Houses.setVisible(false);
		p2Houses.setVisible(false);
		timerPanel.removeAll();
		winnerLabel = new JLabel();
		winnerLabel.setFont(scoreFont);
		// player 1 win
		if (b.player1Score() > b.player2Score()) {
			gameScore = b.player1Score();
			winnerLabel.setText("PLAYER 1 WINS");
			c.gridy = 0;
			timerPanel.add(winnerLabel, c);
			if (networking == 1) {
				SERVER.sendToPlayer(1, "WINNER");
				SERVER.sendToPlayer(2, "LOSER");
			}

			revalidate();
		}
		// player 2 win
		else if (b.player2Score() > b.player1Score()) {
			gameScore = b.player2Score();
			winnerLabel.setText("PLAYER 2 WINS");
			c.gridy = 0;
			timerPanel.add(winnerLabel, c);

			if (networking == 1) {
				SERVER.sendToPlayer(1, "LOSER");
				SERVER.sendToPlayer(2, "WINNER");
			}

			revalidate();
		}
		// tie game
		else {
			gameScore = b.player1Score();
			winnerLabel.setText("TIE GAME");
			timerPanel.add(winnerLabel, c);

			if (networking == 1) {
				SERVER.sendToPlayer(1, "TIE");
				SERVER.sendToPlayer(2, "TIE");
			}

			revalidate();
		}
		// looks for a stored high score
		try {
			Scanner input = new Scanner(file);
			highScore = input.nextInt();
		}
		catch (FileNotFoundException ex) {
			System.out.printf("INPUT ERROR: %s\n", ex);
			highScore = (b.seedsPerPit * b.pitsPerPlayer)/2;
		}
		if (gameScore > highScore) {
			// new high score has been achieved
			try {
				PrintWriter output = new PrintWriter(file);
				output.println(String.valueOf(gameScore));
				output.close();
				c.gridy = 1;
				timerPanel.add(highScoreLabel, c);
			}
			catch (IOException ex) {
				System.out.printf("OUTPUT ERROR: %s\n", ex);
			}
		}
		else {
			// not a new high score
			highScoreLabel.setText("High score: " + String.valueOf(highScore));
			c.gridy = 1;
			timerPanel.add(highScoreLabel, c);
		}
	}

// these functions are used to help build the board
	public static void setArray(int[] array) {
		globalArr = new int[array.length];
		System.arraycopy(array, 0, globalArr, 0, globalArr.length);
		if (globalArr == array) {
			System.out.println("Array copied");
		}
	}
	public static int[] getArray() {
		return globalArr;
	}
	public static void setOptBool(boolean status) {
		optionsFinished = status;
	}
	public static boolean getOptBool() {
		return optionsFinished;
	}
	public static int getAIDifficulty() {
		return difficulty;
	}
	public static void setAIDifficulty(int diff) {
		difficulty = diff;
	}
	public static boolean getPieMove() {
		return pieMove;
	}
	public static void setPieMove(boolean pieStatus) {
		pieMove = pieStatus;
	}

	// Simply calls the title() method
	public Application() {
		title();
	}

	// Sets up the title screen
	public void title() {
		titleFrame = new JFrame();
		titleLabelPanel = new JPanel(new GridBagLayout());
		titleButtonPanel = new JPanel();

		titleFrame.setVisible(true);
		titleFrame.setSize(1280, 720);
		titleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		titleFrame.setTitle("Kalah -- Title Screen");

		titleLabel = new JLabel("TEAM PROJECT 2: KALAH");
		Font titleFont = new Font("Helvetica", Font.BOLD, 50);
		titleLabel.setFont(titleFont);

		nameLabel = new JLabel("Frank DiRosa, Jayton Hopper, and James Vanderburg");
		Font nameFont = new Font("Helvetica", Font.PLAIN, 36);
		nameLabel.setFont(nameFont);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;

		JButton instructionButton = new JButton("Instructions");
		instructionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Instructions button pressed");
				titleFrame.setVisible(false);
				instructions();
			}
		});

		titleSLButton = new JButton("Start server/Start local game");
		titleSLButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("Server/local pressed; options() called");
				titleFrame.setVisible(false);
				options();
			}
		});

		titleClientButton = new JButton("Start client");
		titleClientButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("Client pressed; client() called");
				titleFrame.setVisible(false);
				client();
			}
		});

		titleLabelPanel.add(titleLabel, c);
		c.gridy = 1;
		titleLabelPanel.add(nameLabel, c);
		titleButtonPanel.add(titleSLButton);
		titleButtonPanel.add(titleClientButton);
		titleButtonPanel.add(instructionButton);

		titleFrame.add(titleLabelPanel, BorderLayout.CENTER);
		titleFrame.add(titleButtonPanel, BorderLayout.SOUTH);
	}

	// sets up the instructions screen
	public void instructions() {
		JFrame instructionsFrame = new JFrame();
		JPanel textPanel = new JPanel(new GridBagLayout());
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		JLabel text1 = new JLabel();
		JLabel text2 = new JLabel();
		JLabel text3 = new JLabel();
		JLabel text4 = new JLabel();
		JLabel text5 = new JLabel();
		JLabel text6 = new JLabel();
		JLabel text7 = new JLabel();
		JLabel text8 = new JLabel();

		JButton returnButton = new JButton("Return");

		instructionsFrame.setVisible(true);
		instructionsFrame.setSize(1280, 720);
		instructionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		instructionsFrame.setTitle("Kalah -- Instructions");

		text1.setText("Kalah is an ancient family of board games, and there are many variations." +
		"\n" + "Player 1 houses on the board are on the bottom, and the store is on the right side.");
		text2.setText("Click on a hole to pick up all of the seeds. The game will " +
		"deposit the seeds, one in each hole, until they run out." );
		text3.setText("If you run into your own store, drop a seed in it. If you run into your opponent's store, skip it.");
		text4.setText("If the last seed drops in your store, you get a free turn.");
		text5.setText("If the last seed drops into an empty house on your side, you capture that seed " +
		"and any seeds in the hole directly opposite.");
		text6.setText("The game ends whenever all houses on one side of the board are empty.");
		text7.setText("The player who still has seeds on their side of the board when the game ends captures those seeds.");
		text8.setText("Whoever has the most seeds in their store wins the game.");


		GridBagConstraints c = new GridBagConstraints();

		returnButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("Return button pressed");
				instructionsFrame.setVisible(false);
				title();
			}
		});

		c.gridy=0;
		textPanel.add(text1, c);
		c.gridy=1;
		textPanel.add(text2, c);
		c.gridy=2;
		textPanel.add(text3, c);
		c.gridy=3;
		textPanel.add(text4, c);
		c.gridy=4;
		textPanel.add(text5, c);
		c.gridy=5;
		textPanel.add(text6, c);
		c.gridy=6;
		textPanel.add(text7, c);
		c.gridy=7;
		textPanel.add(text8, c);

		buttonPanel.add(returnButton, c);

		instructionsFrame.add(textPanel, BorderLayout.CENTER);
		instructionsFrame.add(buttonPanel, BorderLayout.SOUTH);
	}

	// sets up client settings screen
	public void client() {
		optionsFrame = new JFrame();
		selectPanel = new JPanel();
		JTextField ip = new JTextField("127.0.0.1");
		JTextField port = new JTextField("5000");
		JLabel playerType = new JLabel("Select type of player:");
		JButton continueButton = new JButton("Continue");

		optionsFrame.setVisible(true);
		optionsFrame.setSize(1280, 720);
		optionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		optionsFrame.setTitle("Kalah -- Choose Client Options");

		String[] playerTypes = {"Human", "AI"};
		JComboBox typeBox = new JComboBox(playerTypes);

		optionsPanel = new JPanel(new GridBagLayout());

		c.gridy = 0;
		optionsPanel.add(ip, c);

		c.gridy = 1;
		optionsPanel.add(port, c);

		c.gridy = 3;
		optionsPanel.add(playerType, c);
		c.gridy = 4;
		optionsPanel.add(typeBox, c);

		networking = 2;
		Application app = this;
		continueButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("client() continue button pressed");
				String ipSelection = ip.getText();
				String portSelection = port.getText();
				String playerSelection = (String) typeBox.getSelectedItem();

				if (playerSelection.startsWith("Human")) {
					CLIENT = new Client(ipSelection, Integer.parseInt(portSelection), false, app);
				}
				else {
					CLIENT = new Client(ipSelection, Integer.parseInt(portSelection), true, app);
				}

				if (CLIENT.isConnected()) {
					optionsFrame.setVisible(false);
				}
			}
		});

		selectPanel = new JPanel();
		selectPanel.add(continueButton);

		optionsFrame.add(optionsPanel, BorderLayout.CENTER);
		optionsFrame.add(selectPanel, BorderLayout.SOUTH);
	}

	// sets up local game/server settings screen
	public void options() {
		c.insets = new Insets(10,10,10,10);
		optionsFrame = new JFrame();
		selectPanel = new JPanel();
		selectOptions = new JButton("Continue");
		// selectServer = new JButton("Start Server");
		// selectClient = new JButton("Start Client");
		houseLabel = new JLabel("Choose number of houses:");
		seedLabel = new JLabel("Choose number of seeds:");
		randomLabel = new JLabel("Should the seeds be randomly distributed?");
		cpuLabel = new JLabel("Choose the level of the CPU:");
		p1Label = new JLabel("Player 1:");
		p2Label = new JLabel("Player 2:");

		optionsFrame.setVisible(true);
		optionsFrame.setSize(1280, 720);
		optionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		optionsFrame.setTitle("Kalah -- Choose Game Options");

		optionsPanel = new JPanel(new GridBagLayout());

		String[] houseOptions = {"4", "5", "6", "7", "8", "9"};
		String[] seedOptions = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
		String[] randomOptions = {"No", "Yes"};
		String[] cpuOptions = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
		String[] playerOptions = {"Human", "AI", "Client"};
		houseBox = new JComboBox(houseOptions);
		seedBox = new JComboBox(seedOptions);
		randomBox = new JComboBox(randomOptions);
		cpuBox = new JComboBox(cpuOptions);
		p1Box = new JComboBox(playerOptions);
		p2Box = new JComboBox(playerOptions);

		JLabel timeLabel = new JLabel ("Enter time limit here:");
		JTextField time = new JTextField("60");
		time.setColumns(3);


		// c.gridx = 0;
		c.gridy = 0;
		optionsPanel.add(houseLabel, c);
		optionsPanel.add(houseBox, c);

		c.gridy = 1;
		optionsPanel.add(seedLabel, c);
		optionsPanel.add(seedBox, c);

		c.gridy = 2;
		optionsPanel.add(randomLabel, c);
		optionsPanel.add(randomBox, c);

		c.gridy = 3;
		optionsPanel.add(cpuLabel, c);
		optionsPanel.add(cpuBox, c);

		c.gridy = 4;
		optionsPanel.add(p1Label, c);
		optionsPanel.add(p1Box, c);

		c.gridy = 5;
		optionsPanel.add(p2Label, c);
		optionsPanel.add(p2Box, c);

		c.gridy = 6;
		optionsPanel.add(timeLabel, c);
		optionsPanel.add(time, c);

		selectPanel.add(selectOptions);
		// selectPanel.add(selectServer);
		// selectPanel.add(selectClient);

		// this runs when continue button is pressed and sets up the game
		selectOptions.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("selectOptions button pressed");
				int houses = Integer.parseInt((String) houseBox.getSelectedItem());
				seeds = Integer.parseInt((String) seedBox.getSelectedItem());
				int cpu = Integer.parseInt((String) cpuBox.getSelectedItem());
				setAIDifficulty(cpu);
				MAXTIME = Integer.parseInt(time.getText());
				timeLeft = MAXTIME;


				String randomSeedsChoice = (String) randomBox.getSelectedItem();
				boolean randomSeeds = (randomSeedsChoice == "Yes");

				String rseeds;
				if (randomSeedsChoice == "Yes") {
					rseeds = "R";
				}
				else {
					rseeds = "N";
				}

				gameType = String.valueOf(houses) + String.valueOf(seeds) + rseeds;
				System.out.println("gameType: " + gameType);

				int[] arr = new int[houses * 2 + 2];
				int p1StoreIndex = houses;
				int p2StoreIndex = 2 * houses + 1;
				if (randomSeeds) {
					boardState = 'R';
					//Randomly distribute seeds to Player 1 side
					int distributedSeeds = houses * seeds / 2;
					for (int i = 0; i < houses - 2; ++i) {
						Random rand = new Random();
						int n = rand.nextInt(seeds) + 1;
						if (distributedSeeds - n <= 0) {
							break;
						}
						arr[i] = n;
						arr[i + p1StoreIndex + 1] = n;
						distributedSeeds -= n;
					}
					arr[p1StoreIndex - 1] = distributedSeeds;
					arr[p2StoreIndex - 1] = distributedSeeds;

				}
				else {
					boardState = 'S';
					for (int i = 0; i < p1StoreIndex; ++i) {
						arr[i] = seeds;
					}
					for (int i = p1StoreIndex + 1; i < p2StoreIndex; ++i) {
						arr[i] = seeds;
					}
				}
				setArray(arr);
				System.out.println("setArray called");
				optionsFrame.setVisible(false);
				game();
			}
		});

		optionsFrame.add(optionsPanel, BorderLayout.CENTER);
		optionsFrame.add(selectPanel, BorderLayout.SOUTH);
	}

	// sets up the GUI for the game
	public void game() {
		c.insets = new Insets(5,5,5,5);
		gameOver = false;
		System.out.println("game() called");

		int[] tempArray = getArray();
		if (b == null) {
			b = new Board(tempArray);
			b.seedsPerPit = seeds;
			System.out.println("seedsPerPit: " + b.seedsPerPit);
			b.boardType = boardState;
			System.out.println("boardType: " + b.boardType);
		}
		tempArray = b.board;

		p1buttons = new ArrayList<JButton>();
		p2buttons = new ArrayList<JButton>();

		p1HouseLabels = new ArrayList<JLabel>();
		p2HouseLabels = new ArrayList<JLabel>();

		gameFrame = new JFrame();
		gameFrame.setTitle("Game");
		gameFrame.setSize(1280, 720);
		gameFrame.setVisible(true);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		int numHouses = b.pitsPerPlayer;

		p1StorePanel = new JPanel(new GridBagLayout());
		p2StorePanel = new JPanel(new GridBagLayout());
		p1Houses = new JPanel(new GridLayout(2, numHouses));
		p2Houses = new JPanel(new GridLayout(2, numHouses));
		timerPanel = new JPanel(new GridBagLayout());

		c.gridy = 0;
		p1Store = new JLabel("0");
        p1Store.setFont(scoreFont);
        p1StorePanel.add(p1Store, c);
		p2Store = new JLabel("0");
        p2Store.setFont(scoreFont);
        p2StorePanel.add(p2Store, c);

		c.gridy = 1;
		JLabel p1 = new JLabel("Player 1");
		p1StorePanel.add(p1, c);
		JLabel p2 = new JLabel("Player 2");
		p2StorePanel.add(p2, c);

		c.gridy = 0;
		timerLabel = new JLabel(String.valueOf(timeLeft));
		timerLabel.setFont(scoreFont);
		timerPanel.add(timerLabel, c);
		c.gridy = 1;
		prevMoveLabel = new JLabel("Previous move: none");
		prevMoveLabel.setFont(moveFont);
		timerPanel.add(prevMoveLabel, c);

		// creates all of the buttons for the houses
		for (int i = 0; i < numHouses; i++) {
            int p2index = (numHouses * 2) - i;
            int index = i;
            JButton tempHouse1 = new JButton("House " + i);
			tempHouse1.setText(String.valueOf(tempArray[i]));
			JLabel tempLabel1 = new JLabel("House " + i);
			tempLabel1.setHorizontalAlignment(SwingConstants.CENTER);

            JButton tempHouse2 = new JButton("House " + p2index);
            tempHouse2.setText(String.valueOf(tempArray[p2index]));
			JLabel tempLabel2 = new JLabel("House " + p2index);
			tempLabel2.setHorizontalAlignment(SwingConstants.CENTER);

			tempHouse1.setActionCommand("House " + i);
            tempHouse1.addActionListener(this);

			tempHouse2.setActionCommand("House " + p2index);
			tempHouse2.addActionListener(this);

            p1buttons.add(tempHouse1);
            p2buttons.add(tempHouse2);
			p1HouseLabels.add(tempLabel1);
			p2HouseLabels.add(tempLabel2);
        }

		System.out.println("p1buttons size: " + p1buttons.size());
		c.insets = new Insets(5,5,5,5);

		for (int i = 0; i < p1buttons.size(); i++) {
            p1Houses.add(p1HouseLabels.get(i), c);
            p2Houses.add(p2buttons.get(i), c);
        }
        for (int i = 0; i < p1buttons.size(); i++) {
            p1Houses.add(p1buttons.get(i), c);
            p2Houses.add(p2HouseLabels.get(i), c);
        }

		gameFrame.add(p1StorePanel, BorderLayout.EAST);
		gameFrame.add(p2StorePanel, BorderLayout.WEST);
		gameFrame.add(p1Houses, BorderLayout.SOUTH);
		gameFrame.add(p2Houses, BorderLayout.NORTH);
		gameFrame.add(timerPanel, BorderLayout.CENTER);
		System.out.println("panels added");
		updateButtons();
		//createBoard();

		if (networking != 2) {
			int players = 0;
			boolean pl1 = false;
			//Create Player 1
			if((String) p1Box.getSelectedItem() == "AI") {
				player1 = new AI(this, b, 1, difficulty);
				System.out.println("P1 Creating AI");
			}
			else if ((String) p1Box.getSelectedItem() == "Human") {
				player1 = new Human(this, b, 1);
				System.out.println("P1 Creating Human");
			}
			else {
				++players;
				pl1 = true;
				player1 = new Human(this, b, 1);
				System.out.println("P1 Creating Client");
			}

			//Create Player 2
			if((String) p2Box.getSelectedItem() == "AI") {
				player2 = new AI(this, b, 2, difficulty);
				System.out.println("P2 Creating AI");
			}
			else if ((String) p2Box.getSelectedItem() == "Human") {
				player2 = new Human(this, b, 2);
				System.out.println("P2 Creating Human");
			}
			else {
				++players;
				player2 = new Human(this, b, 2);
				System.out.println("P2 Creating Client");
			}

			if (players > 0) {
				networking = 1;
				SERVER = new Server(5000, players == 2, this, pl1);
				startServerListener();
			}
		}

		startTimer();
		player1.enable();
		System.out.println("Finished game()");
	}

	// performs moves
	public void performMove(int houseIndex) {
		String moveMsg = "";
		System.out.println("PERFORMING MOVE " + houseIndex);

		if (b.playerMove == 1) {
			if (houseIndex < b.p1KalahIndex || houseIndex == -1) {
				moveMsg = player1.move(houseIndex);
			}
			else {
				return;
			}
		}
		else if (b.playerMove == 2) {
			if (houseIndex > b.p1KalahIndex && houseIndex < b.p2KalahIndex || houseIndex == -1) {
				moveMsg = player2.move(houseIndex);
			}
			else {
				return;
			}
		}

		if (moveMsg != "") {
			timeLeft = MAXTIME;
			
			if (networking == 1) {
				if (b.playerMove == 1) {
					SERVER.sendToPlayer(1, moveMsg);
				}
				else {
					SERVER.sendToPlayer(2, moveMsg);
				}
			}
			else if (networking == 2) {
				try {
					if (b.playerMove == 1 && !CLIENT.isPlayerOne()) {
						CLIENT.sendMessage(moveMsg);
					}
					else if (b.playerMove == 2 && CLIENT.isPlayerOne()) {
						CLIENT.sendMessage(moveMsg);
					}
				} catch (Exception exc) {}
			}
		}

		if (b.allPitsEmpty) {
			System.out.println("end game");
		}
		else {
			if (b.playerMove == 1) {
				player1.enable();
			}
			else {
				player2.enable();
			}
		}
	}

	//Action for all buttons on board
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().contains("House")) {
			int houseIndex = Integer.parseInt(e.getActionCommand().replaceAll("House ",""));
			System.out.println(String.valueOf(houseIndex));
			performMove(houseIndex);
		}
	}

	public static void main(String[] args) {
		new Application();
	}
}
