package net.jmb19905.common;

import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class Chat {

    /**
     * All the clients that participate in this chat.
     */
    private final List<String> clients = new ArrayList<>();

    /**
     * All the messages sent in this chat. Messages are encrypted.
     */
    private final List<Message> messages = new ArrayList<>();

    /**
     * Is true when both participants are online
     */
    private boolean active = false;

    /**
     * Only used on the client
     */
    public EncryptedConnection encryption;

    /**
     * Initializes the EncryptedConnection for the client
     */
    public void initClient(){
        encryption = new EncryptedConnection();
    }

    public void addClient(String name){
        if(clients.size() < 2) {
            clients.add(name);
        }else {
            Logger.log("Group chats are not supported yet!", Logger.Level.WARN);
        }
    }

    public void addMessage(Message message){
        messages.add(message);
    }

    public List<String> getClients() {
        return clients;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public static record Message(String sender, String receiver, String message){}

}
