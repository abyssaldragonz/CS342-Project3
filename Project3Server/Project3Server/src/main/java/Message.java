import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    MessageType type;
    String message;
    String recipient;
    String sender;
    boolean bool;
    int ID;
    int moveRow;
    int moveCol;
    int player;

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

    // server to player message
    public Message(String rec, String mess){
        type = MessageType.TEXT;
        message = mess;
        recipient = rec;
    }

    // player to player message
    public Message(String rec, String sen, String mess){
        type = MessageType.TEXT;
        message = mess;
        sender = sen;
        recipient = rec;
    }

    // creating new room message
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

    // queue message
    public Message(boolean joinedQueue, String name){
        if (joinedQueue) {
            type = MessageType.WAITINGINQUEUE;
            message = name +" is waiting in queue";
            recipient = name;
        } else {
            type = MessageType.LEFTQUEUE;
            message = name +" left the queue";
            recipient = name;
        }
    }

    // rematch message
    public Message(boolean joinedRematch, String rec, String sen){
        type = MessageType.JOINEDREMATCH;
        bool = joinedRematch;
        recipient = rec;
        sender = sen;
    }

    // starting game message
    public Message(boolean gameStarted, String rec, String sen, int gameNum, boolean isFirst){
        if(gameStarted) {
            type = MessageType.GAMESTART;
            message = "You are matched with " + sen + "!";
            sender = sen;
            recipient = rec;
            ID = gameNum;
            bool = isFirst;
        }
    }

    // make move message
    public Message(int gameNum, String sen, int row, int col){
        type = MessageType.MAKEMOVE;
        ID = gameNum;
        moveRow = row;
        moveCol = col;
        sender = sen;
    }
    //same thing as above but with an int player
    public Message(int gameNum, int play, int row, int col){
        type = MessageType.MAKEMOVE;
        ID = gameNum;
        moveRow = row;
        moveCol = col;
        player = play;
    }

    // forfeit message
    public Message(int filler, String sen, String rec) {
        type = MessageType.FORFEIT;
        message = sen + " forfeited. \n You win!";
        sender = sen;
        recipient = rec;
    }
}