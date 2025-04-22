public class GameState {
    // gameboard for connect four
    // -1 = empty piece
    // 0 = first player to join the game
    // 1 = second player to join the game
    private int[][] gameBoard = new int[6][7];
    // the player whose turn it is/just passed
    private int currentPlayer;
    // winState is determined by whether there is a winner or not
    // -1 = no winner
    // 0 = tie
    // 1 = winner
    private int winState;

    // Default constructor for a new gameboard
    public GameState() {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 6; j++)
                gameBoard[i][j] = -1;
        }
        currentPlayer = 0;
        winState = -1;
    }

    // Function to place a game piece
    // @param: int player - the player whose turn it is
    // @param: int placedCol - the column the player placed their piece
    // @return: boolean - whether the piece placement is successful or not
    public boolean placePiece(int player, int placedCol) {
        currentPlayer = player;

        // finding the lowest row the piece can be placed in that row
        int currRow = 6;
        while (currRow >= 0) {
            if (gameBoard[currRow][placedCol] != -1) { // there is already a piece in that position
                currRow--;
            }
            else { // the place is empty so we can place the piece
                gameBoard[currRow][placedCol] = player;
                checkWin(currRow, placedCol, player);
                return true; // piece placement was successful so return true
            }
        }
        return false; // we were unable to place the piece (bc that column was full) so return false
    }

    // Function to check if the piece that was just placed will cause a win
    // @param: int player - the player whose turn it is
    // @param: int placedCol - the column the player placed their piece
    // @return: boolean - whether the game should end or not
    private boolean checkWin(int placedRow, int placedCol, int player) {
        // TODO
        return false;
    }

    // Function to return the gameboard to draw it out in the GUI
    // @return: String[][] - 2D array of Strings
    public int[][] returnBoard() {
        return gameBoard;
    }

    // Function to print the board to console -- for debugging and testing purposes
    private void printBoard() {
        for (int i = 0; i < 7; i++) {
            String row = "";
            for (int j = 0; j < 6; j++)
                row += Integer.toString(gameBoard[i][j]);
            System.out.println(row);
        }
    }
}