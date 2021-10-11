package net.jmb19905.bytethrow.common;

import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.util.Logger;

import java.io.Serializable;
import java.util.*;

public class Chat implements Serializable {

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

    private final UUID uniqueId;

    public Chat(){
        uniqueId = UUID.randomUUID();
    }

    public Chat(UUID uniqueId){
        this.uniqueId = uniqueId;
    }

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

    public void addClients(List<String> names){
        clients.addAll(names);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return active == chat.active && listEqualsIgnoreOrder(clients, chat.clients) && listEqualsIgnoreOrder(messages, chat.messages) && Objects.equals(encryption, chat.encryption) && Objects.equals(uniqueId, chat.uniqueId);
    }

    public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

    @Override
    public int hashCode() {
        return Objects.hash(clients, messages, active, encryption, uniqueId);
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

    public static record Message(String sender, String receiver, String message) implements Serializable{
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Message message1 = (Message) o;
            return Objects.equals(sender, message1.sender) && Objects.equals(receiver, message1.receiver) && Objects.equals(message, message1.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sender, receiver, message);
        }
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

}
