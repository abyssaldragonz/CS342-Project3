import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{
	TextField c1;
	Button b1;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;

	HBox fields;

	ComboBox<Integer> listUsers;
	ListView<String> listItems;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data->{
				Platform.runLater(()->{
					switch (data.type){
						case NEWUSER:
							listUsers.getItems().add(data.recipient);
							listItems.getItems().add(data.recipient + " has joined!");
							break;
						case DISCONNECT:
							listUsers.getItems().remove(data.recipient);
							listItems.getItems().add(data.recipient + " has disconnected!");
							break;
						case TEXT:
							listItems.getItems().add(data.recipient+": "+data.message);
					}
			});
		});
							
		clientConnection.start();

		listUsers = new ComboBox<Integer>();
		listUsers.getItems().add(-1);
		listUsers.setValue(-1);
		listItems = new ListView<String>();

		c1 = new TextField();
		b1 = new Button("Send");
		fields = new HBox(listUsers,b1);
		b1.setOnAction(e->{clientConnection.send(new Message(listUsers.getValue(), c1.getText())); c1.clear();});

		clientBox = new VBox(10, c1,fields,listItems);
		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		// CREATE ACCOUNT GUI START ====================================================================================================
		Text t0 = new Text("Create Account");
		t0.setFont(Font.font("System", 20)); //idk what font we want to change the size
		Text t1 = new Text("username");
		TextField username = new TextField();
		Text t2 = new Text("password");
		TextField password = new TextField();
		Button create = new Button("Create");

		VBox createAccountBox = new VBox(5,t0,new HBox(10, t1), username, new HBox(10, t2), password, create);
		createAccountBox.setMaxWidth(400);
		createAccountBox.setPrefHeight(400);
		createAccountBox.setMaxHeight(400);
		createAccountBox.setAlignment(Pos.CENTER);
		createAccountBox.setStyle("-fx-background-color: #808080; \n");

		StackPane createAccountBackground = new StackPane(createAccountBox);
		createAccountBackground.setStyle("-fx-background-color: #8EF2F5;");

		Scene createAccountScene = new Scene(createAccountBackground, 700 , 700);
		// CREATE ACCOUNT GUI START ====================================================================================================

		primaryStage.setScene(createAccountScene);
//		primaryStage.setScene(new Scene(clientBox, 400, 300));
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}
	


}
