package net.jmb19905.messenger.server;

import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.util.logging.BTMLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * The Connection between two Client from the Servers POV
 * The server will hold the encrypted History of the Client connections so that the client can access them if on a new device of if he lost it
 */
public class E2EConnection {

    private List<Message> history;
    private String username1;
    private String username2;

    public E2EConnection(String username1, String username2){
        this.username1 = username1;
        this.username2 = username2;
        this.history = new ArrayList<>();
    }

    public void addMessage(Message message){
        if(message.sender.equals(username1) || message.sender.equals(username2)){
            history.add(message);
        }else{
            BTMLogger.warn("MessagingServer", "Cannot add to Messages History: invalid usename");
        }
    }

    public void removeMessage(Message message){
        history.remove(message);
    }

    public void close(){
        history = null;
        username1 = null;
        username2 = null;
    }

}
