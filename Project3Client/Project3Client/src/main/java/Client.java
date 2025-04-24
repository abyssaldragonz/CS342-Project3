import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread{
	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;
	private Consumer<Message> callback;
	private String username;

	Client(Consumer<Message> call, String username) {
		callback = call;
		this.username = username;
	}
	//might be able to remove this
	Client(Consumer<Message> call){
		callback = call;
	}
	
	public void run() {
		try {
			socketClient= new Socket("127.0.0.1",5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);

			Message loginMsg = new Message(username, true);
			loginMsg.type = MessageType.NEWUSER; //p sure we dont need this line... too lazy to test tho
			send(loginMsg);

			try {
				Message message = (Message) in.readObject();
				callback.accept(message);

			}
			catch(Exception e) {}
		}
		catch(Exception e) {}
		
		while(true) {
			 
			try {
			Message message = (Message) in.readObject();
			callback.accept(message);
			}
			catch(Exception e) {}
		}
	
    }
	
	public void send(Message data) {
		
		try {
			out.writeObject(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getUsername() {
		return this.username;
	}

}
