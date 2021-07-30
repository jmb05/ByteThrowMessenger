package net.jmb19905.server;

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

    public Chat(){}

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

    public static record Message(String sender, String message){}

}
