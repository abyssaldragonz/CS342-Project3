import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    MessageType type;
    String message;
    String recipient;

    public Message(String name, boolean connect){
        if(connect) {
            type = MessageType.NEWUSER;
            message = name +" has joined!";
            recipient = name;
        } else {
            type = MessageType.DISCONNECT;
            message = name +" has disconnected!";
            recipient = name;
        }
    }

    public Message(String mess){
        type = MessageType.TEXT;
        message = mess;
//        recipient = -1;
    }

    public Message(String rec, String mess){
        type = MessageType.TEXT;
        message = mess;
        recipient = rec;
    }

    public Message(boolean joinedQueue, String name){
        if(joinedQueue) {
            type = MessageType.WAITINGINQUEUE;
            message = name +" is waiting in queue";
            recipient = name;
        } else {
            type = MessageType.LEFTQUEUE;
            message = name +" left the queue";
            recipient = name;
        }
    }

    public Message(boolean gameStarted, String name1, String name2){
        if(gameStarted) {
            type = MessageType.GAMESTART;
            message = " You are matched with " + name2 + "!";
            recipient = name1;
        }
    }

}

