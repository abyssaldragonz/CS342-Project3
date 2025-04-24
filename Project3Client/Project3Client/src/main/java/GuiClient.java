import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color; // https://www.tutorialspoint.com/javafx/javafx_colors.htm
import javafx.scene.shape.Circle;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{
	HashMap<String, Scene> sceneMap;
	Client clientConnection;

	VBox clientBox;
	HBox fields;

	TextField username;
	String currentOpponent;

	Text nameTakenMessage = new Text();

	ListView<String> listItems = new ListView<String>();

	Text profileUsername = new Text("Temp so it doesnt get mad its undefined"); // for profile Section (but needs to be here)

	public static void main(String[] args) {
		launch(args);
	}

	//This is the primary game scene
	//Changes will be made depending on what moves are read
	// A copy of this should probably also be stored on the server side
	private void displayGame(Stage primaryStage) {
		class GamePiece extends Circle {
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
					default:
						setFill(Color.web("#D9D9D9",1.0));
				}
			}
		}

		// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/GridPane.html
		GridPane gameBoard = new GridPane();
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

		Pane gameRoot = new HBox(20, gameBoard, clientBox);
		gameRoot.setStyle("-fx-background-color: white;");

		Scene gameScene = new Scene(gameRoot, 750, 450);
		primaryStage.setTitle("CONNECT FOUR GAME #");
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
		Button joinQueue = new Button("Join queue");
		joinQueue.setOnAction(e -> {
			queueStatus.setText("Waiting for opponent!");
			clientConnection.send(new Message(true, username.getText()));
			joinQueue.setDisable(true);
		});
		Button profileQuit = new Button("Quit");
		profileQuit.setOnAction(e -> {
			if (queueStatus.getText().equals("Waiting for opponent!"))
				clientConnection.send(new Message(false, username.getText()));
			Platform.exit();
			System.exit(0);
		});
		Text profileStatsTitle = new Text("Stats");

		HBox profileButtons = new HBox(joinQueue, profileQuit);
		Button gametest = new Button("game test");
		gametest.setOnAction(e->displayGame(primaryStage));

		Text numWinsText = new Text(" wins");
		Text numLossesText = new Text(" losses");
		Text numTiesText = new Text(" ties");
		VBox profileWinningStatsText = new VBox(profileStatsTitle, numWinsText, numLossesText, numTiesText);
		Text winLossRatioText = new Text("wins:losses");
		HBox profileStats = new HBox(profileWinningStatsText, winLossRatioText);

		VBox mainProfile = new VBox(profileUsername, profileButtons, queueStatus, profileStats, gametest);
		Scene profilePage = new Scene(mainProfile, 700, 700);
		// PROFILE PAGE GUI END ====================================================================================================


		// CREATE ACCOUNT GUI START ====================================================================================================
		Text t0 = new Text("Create Account");
		t0.setFont(Font.font("serif", 20)); //idk what font we want to change the size
		Text t1 = new Text("username");
		username = new TextField();
		Button create = new Button("Create");

		// On create button click
		create.setOnAction(e->{
			// we can setup the connection AFTER the button is pressed
			clientConnection = new Client(data->{
				Platform.runLater(()->{
					switch (data.type){
						case NEWUSER:
//							listItems.getItems().add(data.recipient + " has joined!");
							break;
						case DISCONNECT:
							if (currentOpponent != null && currentOpponent.equals(data.recipient))
								listItems.getItems().add(data.recipient + " has disconnected!");
							break;
						case TEXT:
							listItems.getItems().add(data.sender+": "+ data.message);
							break;
						case GAMESTART:
							listItems.getItems().add(data.message);
							currentOpponent = data.sender;
							queueStatus.setText(""); //reset queue status
							displayGame(primaryStage);
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
								primaryStage.setScene(profilePage);
							}
					}
				});
			}, username.getText()); //send the username from gui to client
			clientConnection.start();
		});

		VBox createAccountBox = new VBox(5,t0,new HBox(10, t1), username, create, nameTakenMessage);
		createAccountBox.setMaxWidth(400);
		createAccountBox.setPrefHeight(400);
		createAccountBox.setMaxHeight(400);
		createAccountBox.setAlignment(Pos.CENTER);
		createAccountBox.setStyle("-fx-background-color: #808080; \n");

		StackPane createAccountBackground = new StackPane(createAccountBox);
		createAccountBackground.setStyle("-fx-background-color: #8EF2F5;");

		Scene createAccountScene = new Scene(createAccountBackground, 700 , 700);
		// CREATE ACCOUNT GUI END ====================================================================================================

		primaryStage.setScene(createAccountScene);
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}
}