package net.jmb19905.bytethrow.common;

import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class Chat {

    /**
     * All the clients that participate in this chat.
     */
    private List<String> clients = new ArrayList<>();

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
    public Encryption encryption;

    /**
     * Initializes the EncryptedConnection for the client
     */
    public void initClient(){
        encryption = new Encryption();
    }

    public void addClient(String name){
        if(clients.size() < 2) {
            clients.add(name);
        }else {
            Logger.warn("Group chats are not supported yet!");
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

    public boolean hasClient(String name){
        return clients.contains(name);
    }

    public void setClients(List<String> clients) {
        this.clients = clients;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "clients=" + clients +
                ", messages=" + messages +
                ", active=" + active +
                ", encryption=" + encryption +
                '}';
    }

    public boolean clientsEquals(Chat chat){
        return chat.getClients().containsAll(clients);
    }

    public static record Message(String sender, String receiver, String message){}

}
