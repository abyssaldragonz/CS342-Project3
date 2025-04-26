public class GameState {
    // gameboard for connect four
    // -1 = empty piece
    // 0 = first player to join the game - yellow
    // 1 = second player to join the game - red
    private int MAXROWS = 6;
    private int MAXCOLS = 7;
    private int[][] gameBoard = new int[MAXROWS][MAXCOLS];
    // the player whose turn it is/just passed
    private int currentPlayer;
    // winState is determined by whether there is a winner or not
    // -1 = no winner
    // 0 = tie
    // 1 = winner
    public int winState;

    // Default constructor for a new gameboard
    public GameState() {
        for (int i = 0; i < MAXROWS; i++) {
            for (int j = 0; j < MAXCOLS; j++)
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
        int currRow = 5;
        while (currRow >= 0) {
            if (gameBoard[currRow][placedCol] != -1) { // there is already a piece in that position
                currRow--;
            }
            else { // the place is empty so we can place the piece
                gameBoard[currRow][placedCol] = player;
                if ( checkWin(currRow, placedCol) ) // someone won so end game
                    winState = 1;
                return true; // piece placement was successful so return true
            }
        }
        return false; // we were unable to place the piece (bc that column was full) so return false
    }

    // Function to check if the piece that was just placed will cause a win
    // @param: int placedRow - the row the player placed their piece
    // @param: int placedCol - the column the player placed their piece
    // @return: boolean - whether the game should end or not
    private boolean checkWin(int placedRow, int placedCol) {
        System.out.println("\nCHECKING CURRENT PLAYER: " + currentPlayer + " PLACED AT ROW " + placedRow + " AND COL " + placedCol);
        // Check horizontal row
        int rowStreak = 0;
        for (int col = 0; col < MAXCOLS; col++) {
            if (gameBoard[placedRow][col] == currentPlayer)
                rowStreak++;
        }
        System.out.println("ROW STREAK " + rowStreak);
        if (rowStreak >= 4)
            return true;

        // Check vertical column
        int colStreak = 0;
        for (int row = 0; row < MAXROWS; row++) {
            if (gameBoard[row][placedCol] == currentPlayer)
                colStreak++;
        }
        System.out.println("COL STREAK " + colStreak);
        if (colStreak >= 4)
            return true;

        // Check major diagonal
        int majorStreak = 0;
        int tempRow = placedRow;
        int tempCol = placedCol;
        while (tempRow >= 0 && tempCol >= 0) { // checking top left
            if (gameBoard[tempRow][tempCol] == currentPlayer)
                majorStreak++;
            tempRow--;
            tempCol--;
        }
            // Offset it so we do not count the placed piece twice
        tempRow = placedRow+1;
        tempCol = placedCol+1;
        while (tempRow < MAXROWS && tempCol < MAXCOLS) { // checking bottom right
            if (gameBoard[tempRow][tempCol] == currentPlayer)
                majorStreak++;
            tempRow++;
            tempCol++;
        }
        System.out.println("MAJOR STREAK " + majorStreak);
        if (majorStreak >= 4)
            return true;

        // Check minor diagonal
        int minorStreak = 0;
        tempRow = placedRow;
        tempCol = placedCol;
        while (tempRow >= 0 && tempCol < MAXCOLS) { // checking top right
            if (gameBoard[tempRow][tempCol] == currentPlayer)
                minorStreak++;
            tempRow--;
            tempCol++;
        }
            // Offset it so we do not count the placed piece twice
        tempRow = placedRow+1;
        tempCol = placedCol-1;
        while (tempRow < MAXROWS && tempCol >= 0) { // checking bottom left
            if (gameBoard[tempRow][tempCol] == currentPlayer)
                minorStreak++;
            tempRow++;
            tempCol--;
        }
        System.out.println("MINOR STREAK " + minorStreak);
        if (minorStreak >= 4)
            return true;

        System.out.println("ROW STREAK " + rowStreak + " COL STREAK " + colStreak);
        System.out.println("MAJOR STREAK " + majorStreak + " MINOR STREAK " + minorStreak);

        return false; // no winning move detected
    }

    // Function to return the gameboard to draw it out in the GUI
    // @return: String[][] - 2D array of Strings
    public int[][] returnBoard() {
        return gameBoard;
    }

    // Function to print the board to console -- for debugging and testing purposes
    public void printBoard() {
        for (int i = 0; i < MAXROWS; i++) {
            String row = "";
            for (int j = 0; j < MAXCOLS; j++)
                row += Integer.toString(gameBoard[i][j]) + "\t";
            System.out.println(row);
        }
    }
}