import java.io.Serializable;


public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    MessageType type;
    String message;
    String recipient;
    String sender;
    boolean bool;
    int ID;
    int move;


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

    // only use this when asking the server a question
    public Message(boolean inpBool){
        type = MessageType.NAMECHECK;
        bool = inpBool;
    }

    //
    public Message(boolean filler, boolean inpBool){
        if (!filler)
            type = MessageType.ROOMWORKS;
        else
            type = MessageType.ROOMNOTOPEN;
        bool = inpBool;
    }

    public Message(String rec, String mess){
        type = MessageType.TEXT;
        message = mess;
        recipient = rec;
    }

    public Message(String rec, String sen, String mess){
        type = MessageType.TEXT;
        message = mess;
        sender = sen;
        recipient = rec;
    }

    public Message(String rec, String sen, String mess, String mess2){
        if (rec.equals("Server")) {
            if (mess2.equals("NEWROOM")) {
                type = MessageType.NEWROOMCODE;
            }
            if (mess2.equals("JOINROOM")) {
                type = MessageType.JOINPRIVATE;
            }
        }
        else {
            type = MessageType.TEXT;
        }
        message = mess;
        sender = sen;
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

    public Message(boolean gameStarted, String rec, String sen, int gameNum){
        if(gameStarted) {
            type = MessageType.GAMESTART;
            message = "You are matched with " + sen + "!";
            sender = sen;
            recipient = rec;
            ID = gameNum;
        }
    }
    public Message(int gameNum, String sen, int row){
        ID = gameNum;
        move = row;
        type = MessageType.MAKEMOVE;
        sender = sen;

    }
}