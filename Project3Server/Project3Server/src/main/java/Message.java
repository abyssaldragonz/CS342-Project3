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

    public Message(String name, boolean joinedQueue, String filler){
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
}

