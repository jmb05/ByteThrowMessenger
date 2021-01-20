package net.jmb19905.messenger.client;

import net.jmb19905.messenger.crypto.EncryptedConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * The ChatHistory of a Chat between two Users - used to save/load the ChatHistory
 */
public class ChatHistory {

    private final String name;
    private EncryptedConnection encryptedConnection;
    private final List<String> messages;

    public ChatHistory() {
        this("", null);
    }

    public ChatHistory(String name, EncryptedConnection encryptedConnection) {
        this.name = name;
        this.encryptedConnection = encryptedConnection;
        this.messages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public EncryptedConnection getNode() {
        return encryptedConnection;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String senderName, String type, String message) {
        messages.add(senderName + ":" + type + ":" + message);
    }

    public void setNode(EncryptedConnection encryptedConnection) {
        this.encryptedConnection = encryptedConnection;
    }

    @Override
    public String toString() {
        return "ChatHistory{" +
                "name='" + name + '\'' +
                ", encryptedConnection=" + encryptedConnection +
                ", messages=" + messages +
                '}';
    }
}
