import java.util.*;

public class Board {
    public static final int NUM_PITS = 6;

    // These arrays represent the rows of pits on either side of the board.
    // The ints in the arrays represent the number of seeds per pit.
    // Visual representation:
    /**************************************************
                      CPU
                5  4  3  2  1  0
            CS                    PS
                0  1  2  3  4  5
                    PLAYER
    **************************************************/
    // The board will be laid out like this since the seeds are distributed in
    // a clockwise manner. We can iterate through the arrays in increasing order
    // and pits from opposite sides whose indices sum to 5 are directly opposite
    // each other.
    private int[] playerPits;
    private int[] cpuPits;

    // these ints represent the number of seeds in the stores. This equates to
    // keeping track of the scores.
    private int playerStore = 0;
    private int cpuStore = 0;

    // constructor
    public Board() {
        playerPits = new int[NUM_PITS];
        cpuPits = new int[NUM_PITS];

        // arrays are the same length, so both are initialized here
        for (int i = 0; i < NUM_PITS; i++) {
            playerPits[i] = 4;
            cpuPits[i] = 4;
        }
    }

    // functions for taking input (take input through mouse clicks)

    

    // checking if pit selection is empty
    // TODO: figure out how to represent "selection"
    bool emptyPit(int selection) {
        if (selection == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    // functions for moving seeds
    void moveSeeds(int selection) {
        bool checkEmpty = emptyPit(selection);
    }

    // functions for scoring
    // Scoring is kept track of in the playerStore and cpuStore variables.
    // Handle scoring changes in moveSeeds()?



    // function for free turn



    // function for checking if last move is into an empty pit on player's side



    // function for checking if board is empty on one side (game ends)
    int getPlayerSeeds() {
        int numSeeds = 0;
        for (int i = 0; i < NUM_PITS; i++) {
            numSeeds += playerPits[i];
        }
        return numSeeds;
    }

    int getCpuSeeds() {
        int numSeeds = 0;
        for (int i = 0; i < NUM_PITS; i++) {
            numSeeds += cpuPits[i];
        }
        return numSeeds;
    }

    int emptySide() {
        // return 0 for empty player side, 1 for empty cpu side, -1 for neither
        int playerSeeds = getPlayerSeeds();
        int cpuSeeds = getCpuSeeds();

        if (playerSeeds == 0) {
            return 0;
        }
        else if (cpuSeeds == 0) {
            return 1;
        }
        else {
            return -1;
        }
    }

    bool isGameOver() {
        int status = emptySide();
        int cpuSeedsLeft = 0;
        int playerSeedsLeft = 0;
        if (status == 0) {
            System.out.println("Player side empty, game over");
            for (int i = 0; i < NUM_PITS; i++;) {
                cpuSeedsLeft += cpuPits[i];
            }
            cpuStore += cpuSeedsLeft;
            return true;
        }
        else if (status == 1) {
            System.out.println("Computer side empty, game over");
            for (int i = 0; i < NUM_PITS; i++;) {
                playerSeedsLeft += playerPits[i];
            }
            playerStore += playerSeedsLeft;
            return true;
        }
        else {
            return false;
        }
    }

    // main

    public static void main(String[] args) {
        Board board = new Board();
    }
}
