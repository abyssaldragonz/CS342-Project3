import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.util.Pair;
import javafx.application.Platform;
import javafx.scene.control.ListView;
public class Server{

	int count = 1;	//number of clients
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Message> callback;
	HashMap<Integer, GuiGame> currentGames = new HashMap<>();
	ArrayList<ClientThread> waitingQueue = new ArrayList<>();
	HashMap<String, ClientThread> privateRooms = new HashMap<>();
	int gameNumber = 0;

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
				// puts every new client thread in the arrayList
				ClientThread c = new ClientThread(mysocket.accept(), count);
				c.start();
				count++;
			    }
			}//end of try
				catch(Exception e) {
					callback.accept(new Message("Server","Server did not launch"));
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
						//if its meant for a client
						for(ClientThread t: clients){
							if(message.recipient.equals(t.username)) {
								try {
									t.out.writeObject(message);
								} catch (Exception e) {
									System.err.println("text Error");
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
								System.err.println("Disconnect Error");
							}
						}
					break;
					case WAITINGINQUEUE:
						//add to our waiting queue
						if (!waitingQueue.contains(this))
							waitingQueue.add(this);

						// remove from private rooms
						privateRooms.entrySet().removeIf(entry -> entry.getValue() == this);

						if (waitingQueue.size() >= 2) {
							ClientThread player1 = waitingQueue.remove(0);
							ClientThread player2 = waitingQueue.remove(0);
							currentGames.put(gameNumber, new GuiGame(player1, player2));
							//Notify users that the games have started
							Message pairMessage1 = new Message(true, player1.username, player2.username, gameNumber, true);
							Message pairMessage2 = new Message(true, player2.username, player1.username, gameNumber, false);

							gameNumber++;

							//remove them from the hashmap of room codes if they are in it
							privateRooms.entrySet().removeIf(entry -> entry.getValue() == player1 || entry.getValue() == player2);

							//send out the messages
							try {
								player1.out.writeObject(pairMessage1);
								player2.out.writeObject(pairMessage2);
								callback.accept(new Message(true, player1.username, player2.username, gameNumber - 1, true));
							} catch (Exception e) {
								System.err.println("New User Error");
							}

							callback.accept(new Message("Server", player1.username + " matched with " + player2.username + " with game number " + gameNumber));

							Message currentMoveMessage = new Message("Server", "Server", player1.username + " is making a move!");
							try {
								player1.out.writeObject(currentMoveMessage);
								player2.out.writeObject(currentMoveMessage);
							} catch (Exception e) {
								System.err.println("Failed to send current move message");
							}
							callback.accept(new Message("Server", player1.username + " is making a move"));
						}
					break;
					case LEFTQUEUE:
						waitingQueue.remove(this);
					break;
					case NEWROOMCODE:
						Message isRoomCodeFreeMessage;
						if(privateRooms.containsKey(message.message) || privateRooms.containsValue(this)) {
							isRoomCodeFreeMessage = new Message(false, false);
						} else {
							isRoomCodeFreeMessage = new Message(false, true);
							privateRooms.put(message.message, this);
							callback.accept(new Message("Server", message.sender + " started a room with code: " + message.message));
						}

						for(ClientThread t : clients) {
							if (t.username.equals(message.sender)) {
								try {
									t.out.writeObject(isRoomCodeFreeMessage);
								} catch (Exception e) {
									System.err.println("Error message write Error");
								}
							}
						}
					break;
					case JOINPRIVATE:
						if (privateRooms.containsKey(message.message)) {
							ClientThread roomHost = privateRooms.get(message.message);
							currentGames.put(gameNumber, new GuiGame(roomHost, this));

							//let them know the game started
							Message pairMessage1 = new Message(true, roomHost.username, this.username, gameNumber, true);
							Message pairMessage2 = new Message(true, this.username, roomHost.username, gameNumber, false);

							gameNumber++;
							//send out the messages
							try {
								roomHost.out.writeObject(pairMessage1);
								this.out.writeObject(pairMessage2);
								callback.accept(new Message(true, roomHost.username, this.username, gameNumber - 1, true));

							} catch (Exception e) {
								System.err.println("Error starting private room");
							}

							// remove their room code
							privateRooms.remove(message.message);
							callback.accept(new Message("Server", this.username + " joined " + roomHost.username + "'s private room with game number " + gameNumber));
						} else {
							Message roomFail = new Message(true, false);
							try {
								this.out.writeObject(roomFail);
							} catch (Exception e) {
								System.err.println("Failed to send join failure");
							}
						}
					break;
					case MAKEMOVE:
						GuiGame game = currentGames.get(message.ID);
						if (game == null) return;
						int column = message.moveCol;
						int row = message.moveRow;
						String currPlayer = message.sender;
						String oppoPlayer = game.player1.username;
						if (game.player1.username.equals(currPlayer))
							oppoPlayer = game.player2.username;



						//return if the wrong player is trying to move
						int movingPlayer = game.gameState.playerToInt.get(currPlayer);
						if (movingPlayer != game.gameState.currentPlayer) return;

						// sending a message to flip the player if the move was successful
						Message currentMoveMessage = new Message("Server", "Server", oppoPlayer + " is making a move!");
						if (game.gameState.placePiece(game.gameState.playerToInt.get(currPlayer), column)) {
							try {
								game.player1.out.writeObject(currentMoveMessage);
								game.player2.out.writeObject(currentMoveMessage);
							} catch (Exception e) {
								System.err.println("Failed to send current move message");
							}

							callback.accept(new Message("Server", currPlayer + " is making a move"));
						}


						game.sendMoveToPlayers(new Message(message.ID, game.gameState.playerToInt.get(currPlayer), row, column));
						int didWin = game.gameState.winState;
						if (didWin == 0) { // tie
							try {
								Message tieMsg = new Message("Server", "Server", "You tied!");
								game.player1.out.writeObject(tieMsg);
								game.player2.out.writeObject(tieMsg);
								callback.accept(new Message("Server", game.player1.username + " tied with " + game.player2.username));
							} catch (Exception e) {
								System.err.println("Error sending tie message");
							}
							currentGames.remove(message.ID);
						}
						else if (didWin == 1) { // currPlayer was the winner
							try {
								// Figure out who is winner
								Server.ClientThread winner = game.gameState.currentPlayer == 0 ? game.player1 : game.player2;
								Server.ClientThread loser  = game.gameState.currentPlayer == 0 ? game.player2 : game.player1;

								Message winMsg = new Message("Server","Server","You win!");
								Message loseMsg = new Message("Server","Server", "You lose.");

								winner.out.writeObject(winMsg);
								loser.out.writeObject(loseMsg);

								callback.accept(new Message("Server", winner.username + " won the game and " + loser.username + " lost."));
							} catch (Exception e) {
								System.err.println("Error sending win/lose messages");
							}
							currentGames.remove(message.ID);
						}
						else {
							//switch the current player
							game.gameState.currentPlayer = (game.gameState.currentPlayer == 0) ? 1 : 0;
						}
						break;
					case JOINEDREMATCH:
						String rematchKey = (message.recipient.compareTo(message.sender) < 0) ? message.recipient + message.sender : message.sender + message.recipient;
						if (message.bool) {
							if (privateRooms.containsKey(rematchKey)) {
								ClientThread roomHost = privateRooms.get(rematchKey);
								currentGames.put(gameNumber, new GuiGame(roomHost, this));

								//let them know the game started
								Message pairMessage1 = new Message(true, roomHost.username, this.username, gameNumber, true);
								Message pairMessage2 = new Message(true, this.username, roomHost.username, gameNumber, false);

								gameNumber++;
								//send out the messages
								try {
									roomHost.out.writeObject(pairMessage1);
									this.out.writeObject(pairMessage2);
									callback.accept(new Message(true, roomHost.username, this.username, gameNumber - 1, true));
								} catch (Exception e) {
									System.err.println("Error starting rematch");
								}

								// remove their room code
								privateRooms.remove(rematchKey);
								callback.accept(new Message("Server", this.username + " rematched " + roomHost.username + "with game number " + gameNumber));

							} else {
								privateRooms.put(rematchKey, this);
								for(ClientThread t : clients) {
									if (t.username.equals(message.recipient) || t.username.equals(message.sender)) {
										try {
											t.out.writeObject(message);
										} catch (Exception e) {
											System.err.println("rematch Error");
										}
									}
								}
							}
						} else {
                            privateRooms.remove(rematchKey);
							for(ClientThread t : clients) {
								if (t.username.equals(message.recipient) || t.username.equals(message.sender)) {
									try {
										t.out.writeObject(message);
									} catch (Exception e) {
										System.err.println("rematch Error");
									}
								}
							}
						}
						break;
					case FORFEIT:
						callback.accept(new Message("Server", message.sender + " forfeited the match and " + message.recipient + "wins."));
						for(ClientThread t : clients) {
							if (t.username.equals(message.recipient)) {
								try {
									t.out.writeObject(message);
								} catch (Exception e) {
									System.err.println("rematch Error");
								}
							}
						}
				}

			}
			
			public void run(){
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);

					//FOR SOME REASON WHEN YOU REMOVE THIS IT SAYS NULL LEFT THE GAME IDK WHY.
					Message firstMsg = (Message) in.readObject();
					if (firstMsg.type == MessageType.NEWUSER)
					{
						//lets see if the username is taken
						boolean isTaken = false;
						for (ClientThread c : clients) {
							if (c.username != null && c.username.equals(firstMsg.recipient)) {
								isTaken = true;
								break;
							}
						}
						if (isTaken) {
							Message error = new Message(false);
							try {
								out.writeObject(error);
								connection.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							connection.close();
							return;
						}
						this.username = firstMsg.recipient;
						clients.add(this);
						Message success = new Message(true);
						try {
							out.writeObject(success);
						} catch (Exception e) {
							e.printStackTrace();
						}

						Message joinedMsg = new Message(username, true);
						callback.accept(joinedMsg);
						updateClients(joinedMsg);
					}
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}
					
				 while(true) {
					    try {
					    	Message data = (Message) in.readObject();
					    	callback.accept(data);
							updateClients(data);
						}
					    catch(Exception e) {
							e.printStackTrace();
							Message discon = new Message(username, false);
					    	callback.accept(discon);
					    	updateClients(discon);
					    	clients.remove(this);
					    	break;
					    }
					}
				}//end of run
			
			
		}//end of client thread
}


	
	

	
