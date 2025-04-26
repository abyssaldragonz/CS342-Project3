import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MyTest {

	@Test
	void GameStateCreation() {
		System.out.println("\nTESTING EMPTY BOARD");
		GameState newGame = new GameState();
		newGame.printBoard();
		assertEquals(-1, newGame.winState, "Incorrect initialization of empty gameboard");
	}

	@Test
	void GameStateFilledCol() {
		System.out.println("\nTESTING FILLED COLUMN BOARD");
		GameState newGame = new GameState();
		assertTrue(newGame.placePiece(0, 0));
		assertTrue(newGame.placePiece(1, 0));
		assertTrue(newGame.placePiece(0, 0));
		assertTrue(newGame.placePiece(1, 0));
		assertTrue(newGame.placePiece(0, 0));
		assertTrue(newGame.placePiece(1, 0));
		// overflowing the column
		assertFalse(newGame.placePiece(0, 0));
		assertFalse(newGame.placePiece(1, 0));
		assertFalse(newGame.placePiece(0, 0));
		assertFalse(newGame.placePiece(1, 0));
		newGame.printBoard();
		assertEquals(-1, newGame.winState, "Incorrect win state of game");
	}

	@Test
	void GameStateMidGame() {
		System.out.println("\nTESTING MID GAME");
		GameState newGame = new GameState();
		newGame.placePiece(0, 0);
		newGame.placePiece(1, 1);
		newGame.placePiece(0, 1);
		newGame.placePiece(1, 5);
		newGame.placePiece(0, 0);
		newGame.placePiece(1, 6);
		newGame.placePiece(0, 1);
		newGame.placePiece(1, 2);
		newGame.placePiece(0, 5);
		newGame.placePiece(1, 6);
		newGame.placePiece(0, 5);
		newGame.placePiece(1, 5);
		newGame.printBoard();
		assertEquals(-1, newGame.winState, "Incorrect win state in the middle of a game");
	}

	@Test
	void GameStateRowWin() {
		System.out.println("\nTESTING ROW WIN");
		GameState newGame = new GameState();
		assertTrue(newGame.placePiece(1, 0));
		assertTrue(newGame.placePiece(0, 2));
		assertTrue(newGame.placePiece(0, 1));
		assertTrue(newGame.placePiece(0, 4));
		assertTrue(newGame.placePiece(0, 3));
		newGame.printBoard();
		assertEquals(1, newGame.winState, "Incorrect win state of game");
		assertEquals(0, newGame.currentPlayer, "Incorrect row winner");
	}

	@Test
	void GameStateColWin() {
		System.out.println("\nTESTING COL WIN");
		GameState newGame = new GameState();
		assertTrue(newGame.placePiece(0, 0));
		assertTrue(newGame.placePiece(1, 0));
		assertTrue(newGame.placePiece(1, 0));
		assertTrue(newGame.placePiece(1, 0));
		assertTrue(newGame.placePiece(1, 0));
		newGame.printBoard();
		assertEquals(1, newGame.winState, "Incorrect win state of game");
		assertEquals(1, newGame.currentPlayer, "Incorrect column winner");
	}

	@Test
	void GameStateMajorWin() {
		System.out.println("\nTESTING MAJOR WIN");
		GameState newGame = new GameState();
		newGame.placePiece(0, 0);
		newGame.placePiece(0, 1);
		newGame.placePiece(0, 1);
		newGame.placePiece(1, 2);
		newGame.placePiece(1, 2);
		newGame.placePiece(0, 2);
		newGame.placePiece(1, 3);
		newGame.placePiece(1, 1);
		newGame.placePiece(1, 1);
		newGame.placePiece(0, 0);
		newGame.placePiece(0, 0);
		newGame.placePiece(1, 0);
		newGame.printBoard();
		assertEquals(1, newGame.winState, "Incorrect major win");
		assertEquals(1, newGame.currentPlayer, "Incorrect major winner");
	}

	@Test
	void GameStateMinorWin() {
		System.out.println("\nTESTING MINOR WIN");
		GameState newGame = new GameState();
		newGame.placePiece(0, 0);
		newGame.placePiece(0, 1);
		newGame.placePiece(0, 1);
		newGame.placePiece(1, 2);
		newGame.placePiece(1, 2);
		newGame.placePiece(0, 2);
		newGame.placePiece(1, 3);
		newGame.placePiece(1, 3);
		newGame.placePiece(1, 3);
		newGame.placePiece(0, 3);
		newGame.printBoard();
		assertEquals(1, newGame.winState, "Incorrect minor win");
		assertEquals(0, newGame.currentPlayer, "Incorrect minor winner");
	}

	@Test
	void GameStateTie() {
		System.out.println("\nTESTING TIE");
		GameState newGame = new GameState();
		int currPiece = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 7; j++) {
				assertTrue(newGame.placePiece(currPiece, j));
				if (currPiece == 0)
					currPiece = 1;
				else
					currPiece = 0;
			}
		}
		newGame.printBoard();
		assertEquals(0, newGame.winState, "Incorrect tie state");
		assertEquals(-1, newGame.currentPlayer, "Incorrect no winner");
	}


}
