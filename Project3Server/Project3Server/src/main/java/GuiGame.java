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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiGame extends Application{

	Server serverConnection;

	Server.ClientThread player1;
	Server.ClientThread player2;

	//An instance of a class that tracks the current gameState
	GameState gameState;

	public GuiGame(Server.ClientThread p1, Server.ClientThread p2){
		player1 = p1;
		player2 = p2;
		gameState = new GameState(player1, player2);
	}

	public void sendMoveToPlayers(Message msg) {
		try {
			player1.out.writeObject(msg);
			player2.out.writeObject(msg);
		} catch (Exception e) {
			System.err.println("Failed to send to players");
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
	}



}
