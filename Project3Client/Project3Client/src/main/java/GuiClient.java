import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color; // https://www.tutorialspoint.com/javafx/javafx_colors.htm
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{
	HashMap<String, Scene> sceneMap;
	Client clientConnection;

	GridPane gameBoard;

	VBox clientBox;
	HBox fields;

	TextField username;
	String currentOpponent;

	Text nameTakenMessage = new Text();

	ListView<String> listItems = new ListView<String>();

	Text profileUsername = new Text("Temp so it doesnt get mad its undefined"); // for profile Section (but needs to be here)

	boolean myTurn = false;
	int myPlayerNumber = -1;

	public static void main(String[] args) {
		launch(args);
	}

	private void showInGamePopup(Stage primaryStage, Scene profilePage, String result) {
		StackPane popup = new StackPane();
		popup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
		popup.setPrefSize(400, 400);

		Text resultText = new Text(result);
		resultText.setStyle("-fx-font-size: 48px; -fx-fill: white;");

		Button backButton = new Button("Back to Profile");
		backButton.setOnAction(e -> {
			primaryStage.setTitle("USER PROFILE @" + username.getText());
			primaryStage.setScene(profilePage);
		});

		Button playAgain = new Button("Rematch(0/2)");
		playAgain.setOnAction(e -> {

		});

		VBox popupContent = new VBox(20, resultText, backButton, playAgain);
		popupContent.setAlignment(Pos.CENTER);

		popup.getChildren().add(popupContent);

		((Pane) clientBox.getParent()).getChildren().add(popup); // Adds it on top
	}


	private class GamePiece extends Circle {
		private int color;
		private int row;
		private int col;

		GamePiece(int r, int c) {
			row = r;
			col = c;
			color = -1;
			setFill(Color.web("#D9D9D9",1.0));
			setRadius(25);
		}

		public void changeColor(int newColor) {
			color = newColor;
			switch (color) {
				case 0:
					setFill(Color.YELLOW); // yellow goes first
					break;
				case 1:
					setFill(Color.RED); // red goes second
					break;
				// for the drop box
				case 10:
					setFill(Color.web("#FFFFC5")); // light yellow
					break;
				case 20:
					setFill(Color.web("#FF7F7F")); // light red
					break;
				default:
					setFill(Color.web("#D9D9D9",1.0)); // plain gray
			}
		}
	}

	//This is the primary game scene
	//Changes will be made depending on what moves are read
	// A copy of this should probably also be stored on the server side
	private void displayGame(Stage primaryStage, int gameNumber) {

		// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/GridPane.html
		gameBoard = new GridPane();
		gameBoard.setStyle("-fx-background-color: #53B7F5;\n" +
				"-fx-width: 500;\n"+
				"-fx-height: 500;\n");


		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 7; j++) {
				GamePiece newPiece = new GamePiece(i, j);
				gameBoard.add(newPiece, j, i); // to add, col then row
				gameBoard.setMargin(newPiece, new Insets(7)); // https://docs.oracle.com/javase/8/javafx/api/javafx/geometry/Insets.html
			}
		}

		GridPane userDropbox = new GridPane();
		userDropbox.setStyle("-fx-background-color: #D9D9D9;\n");
		for (int c = 0; c < 7; c++) {
			GamePiece newPiece = new GamePiece(0, c);
			userDropbox.add(newPiece, c, 0); // to add, col then row
			userDropbox.setMargin(newPiece, new Insets(7)); // https://docs.oracle.com/javase/8/javafx/api/javafx/geometry/Insets.html
		}

		// user interaction mechanism
		// https://www.w3resource.com/java-exercises/javafx/javafx-events-and-event-handling-exercise-1.php
		// https://stackoverflow.com/questions/31095954/how-to-get-gridpane-row-and-column-ids-on-mouse-entered-in-each-cell-of-grid-in
		// https://stackoverflow.com/questions/18597939/handle-mouse-event-anywhere-with-javafx
		// https://stackoverflow.com/questions/40943395/how-to-access-a-node-inside-of-a-gridpane-in-java
		// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/input/MouseEvent.html
		// https://www.w3resource.com/java-exercises/javafx/javafx-events-and-event-handling-exercise-1.php

		for (Node child : userDropbox.getChildren()) {
			Integer c = GridPane.getColumnIndex(child);
			int column = c == null ? 0 : c;
			if ( (child instanceof GamePiece)) {
				child.setOnMouseEntered(event -> {
					if (!myTurn) return;
					if (myPlayerNumber == 0) {
						((GamePiece) child).changeColor(10);
					} else {
						((GamePiece) child).changeColor(20);
					}
				});

				child.setOnMouseExited(event -> {
					((GamePiece) child).changeColor(-1);
				});

				child.setOnMousePressed(event -> {
					// Search from bottom row upwards
					for (int row = 5; row >= 0; row--) {
						for (Node piece : gameBoard.getChildren()) {
							Integer pieceRow = GridPane.getRowIndex(piece);
							Integer pieceCol = GridPane.getColumnIndex(piece);
							if (pieceRow == null) pieceRow = 0;
							if (pieceCol == null) pieceCol = 0;

							if (pieceRow == row && pieceCol == column) {
								if (piece instanceof GamePiece && ((GamePiece) piece).color == -1) {
									clientConnection.send(new Message(gameNumber,this.username.getText(),pieceRow, pieceCol));
									return;
								}
							}
						}
					}
				});
			}
		}

		// end

		VBox mainGame = new VBox(userDropbox, gameBoard);

		HBox gameRoot = new HBox(20, mainGame, clientBox);
		gameRoot.setStyle("-fx-background-color: #D9D9D9;");

		Scene gameScene = new Scene(gameRoot, 750, 450);
		primaryStage.setTitle("CONNECT FOUR GAME #" + gameNumber);
		primaryStage.setScene(gameScene);
	}


	@Override
	public void start(Stage primaryStage) throws Exception {

		// STARTER CODE STUFF MOSTLY CLIENT BOX
		TextField c1 = new TextField();
		Button b1 = new Button("Send");
		fields = new HBox(b1);
		b1.setOnAction(e->{
			clientConnection.send(new Message(currentOpponent, username.getText(), c1.getText()));
			listItems.getItems().add(username.getText()+": "+ c1.getText());
			c1.clear();
		});

		clientBox = new VBox(10, c1,fields,listItems);
		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");

		Text queueStatus = new Text();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
				if (queueStatus.getText().equals("Waiting for opponent!"))
					clientConnection.send(new Message(false, username.getText()));
                Platform.exit();
                System.exit(0);
            }
        });


		// PROFILE PAGE GUI START ====================================================================================================
		//moved profileUsername to within create button press bc here it is still nothing
		Button joinQueue = new Button("Join public queue");
		Button startPrivateGame = new Button("Start private game");
		Button joinPrivateGame = new Button("Join private game");
		Button submitRoom = new Button("Submit room");
		submitRoom.setDisable(true);
		Button joinRoom = new Button("Join room");
		joinRoom.setDisable(true);

		TextField createCode = new TextField();
		TextField enterCode = new TextField();

		// styling
		profileUsername.setFont(Font.font("roboto", FontWeight.EXTRA_BOLD, 35));
		nameTakenMessage.setFont(Font.font("roboto", FontWeight.BOLD, 25));
		nameTakenMessage.setFill(Color.RED);
		queueStatus.setFont(Font.font("roboto", FontWeight.BOLD, 25));
		joinQueue.setFont(Font.font("roboto", 15));
		startPrivateGame.setFont(Font.font("roboto", 15));
		joinPrivateGame.setFont(Font.font("roboto", 15));
		submitRoom.setFont(Font.font("roboto", 15));
		joinRoom.setFont(Font.font("roboto", 15));
		createCode.setFont(Font.font("roboto", 15));
		enterCode.setFont(Font.font("roboto", 15));

		joinQueue.setOnAction(e -> {
			queueStatus.setText("Waiting for opponent!");
			clientConnection.send(new Message(true, username.getText()));
			joinQueue.setDisable(true);
		});
		Button profileQuit = new Button("Quit");
		profileQuit.setFont(Font.font("roboto", FontWeight.BOLD, 15));
		profileQuit.setOnAction(e -> {
			if (queueStatus.getText().equals("Waiting for opponent!"))
				clientConnection.send(new Message(false, username.getText()));
			Platform.exit();
			System.exit(0);
		});
		Text profileStatsTitle = new Text("Stats");

		createCode.setPromptText("Create a room password");
		createCode.setDisable(true);
		startPrivateGame.setOnAction(e -> {
			enterCode.setDisable(true);
			createCode.setDisable(false);
			submitRoom.setDisable(true);
			joinRoom.setDisable(true);

			//Leave the queue if you are in it
			if (queueStatus.getText().equals("Waiting for opponent!")) {
				clientConnection.send(new Message(false, username.getText()));
				queueStatus.setText("Left the queue!");
				joinQueue.setDisable(false);
			}
		});

		enterCode.setPromptText("Enter a room password");
		enterCode.setDisable(true);
		joinPrivateGame.setOnAction(e -> {
			createCode.setDisable(true);
			enterCode.setDisable(false);
			submitRoom.setDisable(true);
			joinRoom.setDisable(true);

			//Leave the queue if you are in it
			if (queueStatus.getText().equals("Waiting for opponent!")) {
				clientConnection.send(new Message(false, username.getText()));
				queueStatus.setText("Left the queue!");
				joinQueue.setDisable(false);
			}
		});

		createCode.setOnKeyPressed(e -> {
            submitRoom.setDisable(createCode.getText().isEmpty());
		});
		enterCode.setOnKeyPressed(e -> {
			joinRoom.setDisable(enterCode.getText().isEmpty());
		});

		submitRoom.setOnAction(e -> {
			clientConnection.send(new Message("Server", username.getText(), createCode.getText(), "NEWROOM"));
		});

		joinRoom.setOnAction(e -> {
			clientConnection.send(new Message("Server", username.getText(), enterCode.getText(), "JOINROOM"));
		});

		HBox profileButtons = new HBox(20, new VBox(25,joinQueue, startPrivateGame, joinPrivateGame),new VBox(25,queueStatus, createCode, enterCode), new VBox(25,profileQuit, submitRoom, joinRoom));

		VBox mainProfile = new VBox(10, profileUsername, profileButtons);
		mainProfile.setStyle("-fx-padding: 25px;\n" );
		Scene profilePage = new Scene(mainProfile, 700, 700);
		// PROFILE PAGE GUI END ====================================================================================================


		// CREATE ACCOUNT GUI START ====================================================================================================
		Text welcomeToConnectFour = new Text("CONNECT FOUR");
		welcomeToConnectFour.setFont(Font.font("roboto", FontWeight.EXTRA_BOLD, 55));
		welcomeToConnectFour.setFill(Color.RED);
		Text t0 = new Text("Create Account");
		t0.setFont(Font.font("roboto", 35));
		Text t1 = new Text("username");
		t1.setFont(Font.font("roboto", 25));
		username = new TextField();
		username.setFont(Font.font("roboto", 15));
		Button create = new Button("Create");
		create.setFont(Font.font("roboto", 15));

		// On create button click
		create.setOnAction(e->{
			// check for empty username
			if ( username.getText().isEmpty() ) {
				return;
			}

				// we can setup the connection AFTER the button is pressed
			clientConnection = new Client(data->{
				Platform.runLater(()->{
					switch (data.type){
						case NEWUSER:
//							listItems.getItems().add(data.recipient + " has joined!");
							break;
						case DISCONNECT:
							if (currentOpponent != null && currentOpponent.equals(data.recipient))
							{
								listItems.getItems().add(data.recipient + " has disconnected!");
								showInGamePopup(primaryStage, profilePage, "You Win!");
							}
							break;
						case TEXT:
							listItems.getItems().add(data.sender+": "+ data.message);
							if (data.sender.equals("Server")) {
								if (data.message.equals("You win!")) {
									showInGamePopup(primaryStage, profilePage, "You Win!");
								} else if (data.message.equals("You lose.")) {
									showInGamePopup(primaryStage, profilePage, "You Lose.");
								} else if (data.message.equals("You tied!")) {
									showInGamePopup(primaryStage, profilePage, "It's a Tie.");
								}
							}
							break;
						case GAMESTART:
							listItems.getItems().add(data.message);
							currentOpponent = data.sender;
							queueStatus.setText(""); //reset queue status
							createCode.setText("");
							enterCode.setText("");
							displayGame(primaryStage, data.ID);
							if (data.bool) {
								myPlayerNumber = 0;
								myTurn = true;
							} else {
								myPlayerNumber = 1;
								myTurn = false;
							}
							break;
						case NAMECHECK:
							if (!data.bool) {
								// tell them!
								nameTakenMessage.setText("This username is already taken");
								try {
									clientConnection.socketClient.close();
								} catch (Exception e1) { e1.printStackTrace(); }
							} else {
								profileUsername.setText("@" + username.getText());
								primaryStage.setTitle("USER PROFILE @" + username.getText());
								primaryStage.setScene(profilePage);
							}
							break;
						case ROOMWORKS:
							if (data.bool) {
								if (queueStatus.getText().equals("Waiting for opponent!"))
									clientConnection.send(new Message(false, username.getText()));
								queueStatus.setText("Room created! Waiting for opponent...");
							} else {
								queueStatus.setText("Room code already taken or you already have code");
							}
							break;
						case ROOMNOTOPEN:
							if (data.bool) {
								if (queueStatus.getText().equals("Waiting for opponent!"))
									clientConnection.send(new Message(false, username.getText()));
								queueStatus.setText("Joining room");
							} else {
								queueStatus.setText("Incorrect room code");
							}
							break;
						case MAKEMOVE:
							int row = data.moveRow;
							int col = data.moveCol;
							int player = data.player;

							// Update the board
							for (Node piece : gameBoard.getChildren()) {
								Integer pieceRow = GridPane.getRowIndex(piece);
								Integer pieceCol = GridPane.getColumnIndex(piece);
								if (pieceRow == null) pieceRow = 0;
								if (pieceCol == null) pieceCol = 0;

								if (pieceRow == row && pieceCol == col) {
									if (piece instanceof GamePiece) {
										((GamePiece) piece).changeColor(player);
									}
								}
							}
							// flip the player turn
							if (player != myPlayerNumber)
								myTurn = true;
							else
								myTurn = false;
							break;
					}
				});
			}, username.getText()); //send the username from gui to client
			clientConnection.start();
		});

		VBox createAccountBox = new VBox(15,welcomeToConnectFour, new Rectangle(10,35, Paint.valueOf("#D9D9D9")), t0, new HBox(10, t1), username, create, nameTakenMessage);
		createAccountBox.setMaxWidth(400);
		createAccountBox.setPrefHeight(400);
		createAccountBox.setMaxHeight(400);
		createAccountBox.setAlignment(Pos.CENTER);
		createAccountBox.setStyle("-fx-background-color: #D9D9D9; \n-fx-padding: 15px");

		StackPane createAccountBackground = new StackPane(createAccountBox);
		createAccountBackground.setStyle("-fx-background-color: #8EF2F5;");

		Scene createAccountScene = new Scene(createAccountBackground, 700 , 700);
		// CREATE ACCOUNT GUI END ====================================================================================================

		primaryStage.setScene(createAccountScene);
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}
}