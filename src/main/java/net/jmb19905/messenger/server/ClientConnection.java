package net.jmb19905.messenger.server;

import net.jmb19905.messenger.crypto.Node;

public class ClientConnection {

    private String username = null;
    private boolean isLoggedIn;
    private final Node node;

    public ClientConnection(Node node, boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        this.node = node;
    }

    public ClientConnection(String username, Node node, boolean isLoggedIn) {
        this.username = username;
        this.isLoggedIn = isLoggedIn;
        this.node = node;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public Node getNode() {
        return node;
    }
}
