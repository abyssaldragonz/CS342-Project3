import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Message> callback;
	
	
	Server(Consumer<Message> call){
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread{
		
		public void run() {
		
			try(ServerSocket mysocket = new ServerSocket(5555);){
		    System.out.println("Server is waiting for a client!");
		  
			
		    while(true) {
		
				ClientThread c = new ClientThread(mysocket.accept(), count);
				clients.add(c);
				c.start();
				
				count++;
				
			    }
			}//end of try
				catch(Exception e) {
					callback.accept(new Message("Server did not launch"));
				}
			}//end of while
		}
	

		class ClientThread extends Thread{
			Socket connection;
			int count; //might not need tbh
			ObjectInputStream in;
			ObjectOutputStream out;
			String username;
			
			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;	
			}
			
			public void updateClients(Message message) {
				switch(message.type){
					case TEXT:
						for(ClientThread t: clients){
							if(message.recipient.equals("NEED TO FIX.")) {
								try {
									t.out.writeObject(message);
								} catch (Exception e) {
									System.err.println("New User Error");
								}
							}
						}
					break;
					case NEWUSER:
						for(ClientThread t : clients) {
							if(this != t) {
								try {
									t.out.writeObject(message);
								} catch (Exception e) {
									System.err.println("New User Error");
								}
							}
						}
					break;
					case DISCONNECT:
						for(ClientThread t : clients) {
							try {
								t.out.writeObject(message);
							} catch (Exception e) {
								System.err.println("New User Error");
							}
						}

				}

			}
			
			public void run(){
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);

					Message firstMsg = (Message) in.readObject();
					if (firstMsg.type == MessageType.NEWUSER)
					{
						this.username = firstMsg.recipient;
						Message joinedMsg = new Message(username, true);
						callback.accept(joinedMsg);
						updateClients(joinedMsg);
					}
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}
				
				updateClients(new Message("NEEDTOFIXTHISIDKWWAHTTHISIS",true));
					
				 while(true) {
					    try {
					    	Message data = (Message) in.readObject();
					    	callback.accept(data);
							updateClients(data);
						}
					    catch(Exception e) {
							e.printStackTrace();
							Message discon = new Message("NEEDTOFIX", false);
					    	callback.accept(discon);
					    	updateClients(discon);
					    	clients.remove(this);
					    	break;
					    }
					}
				}//end of run
			
			
		}//end of client thread
}


	
	

	
