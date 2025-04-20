import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    MessageType type;
    String message;
    String recipient;

    public Message(String name, boolean connect){
        if(connect) {
            type = MessageType.NEWUSER;
            message = "User "+name+" has joined!";
            recipient = name;
        } else {
            type = MessageType.DISCONNECT;
            message = "User "+name+" has disconnected!";
            recipient = name;
        }
    }

    public Message(String mess){
        type = MessageType.TEXT;
        message = mess;
//        recipient = -1;
    }

    public Message(int rec, String mess){
        type = MessageType.TEXT;
        message = mess;
//        recipient = rec;
    }
}

