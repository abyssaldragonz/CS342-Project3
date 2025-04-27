
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiServer extends Application{

	Server serverConnection;
	
	ListView<String> listItems;
	ListView<String> listUsers;

	HashMap<Integer, ListView<String>> gameMoves = new HashMap<>();
	VBox gamesBox = new VBox(10);

	HBox lists;
	
	public static void main(String[] args) {
		launch(args);
	}


	@Override
	public void start(Stage primaryStage) throws Exception {
		serverConnection = new Server(data->{
			Platform.runLater(()->{
				switch (data.type){
					case TEXT:
						listItems.getItems().add(data.recipient+": "+data.message);
						break;
					case NEWUSER:
						listUsers.getItems().add(data.recipient);
						listItems.getItems().add(data.recipient + " has joined!");
						break;
					case DISCONNECT:
						listUsers.getItems().remove(data.recipient);
						listItems.getItems().add(data.recipient + " has disconnected!");
						break;
					case WAITINGINQUEUE:
						listItems.getItems().add(data.message);
						break;
					case LEFTQUEUE:
						listItems.getItems().add(data.message);
						break;
					case GAMESTART:
						ListView<String> moveList = new ListView<>();
						moveList.getItems().add("Game #" + data.ID + " Moves:");
						gameMoves.put(data.ID, moveList);
						gamesBox.getChildren().add(moveList);
						break;
					case MAKEMOVE:
						if (gameMoves.containsKey(data.ID)) {
							gameMoves.get(data.ID).getItems().add(data.sender + " moved a (row " + data.moveRow + ", col " + data.moveCol + ")");
						}
					case FORFEIT:
						if (gameMoves.containsKey(data.ID)) {
							listItems.getItems().add(data.recipient + " has forfeited!");
						}
				}
			});
		});

		
		listItems = new ListView<String>();
		listUsers = new ListView<String>();

		lists = new HBox(20, new VBox(new Text("PLAYERS"), listUsers), new VBox(new Text("ACTIVITY"), listItems), new VBox(new Text("GAMES"), gamesBox));


		//we could also make a listItems for seperate games
		//to keep track of the moves


		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: coral");

		pane.setCenter(lists);
		pane.setStyle("-fx-font-family: 'serif'");
		;

		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(new Scene(pane, 700, 700));
		primaryStage.setTitle("Server GUI!");
		primaryStage.show();
		
	}

}
