package net.jmb19905.messenger.client;

import net.jmb19905.messenger.crypto.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * The ChatHistory of a Chat between two Users - used to save/load the ChatHistory
 */
public class ChatHistory {

    private final String name;
    private Node node;
    private final List<String> messages;

    public ChatHistory() {
        this("", null);
    }

    public ChatHistory(String name, Node node) {
        this.name = name;
        this.node = node;
        this.messages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String senderName, String message) {
        messages.add(senderName + ":" + message);
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "ChatHistory{" +
                "name='" + name + '\'' +
                ", node=" + node +
                ", messages=" + messages +
                '}';
    }
}
