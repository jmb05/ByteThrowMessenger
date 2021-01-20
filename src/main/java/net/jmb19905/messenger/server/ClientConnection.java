package net.jmb19905.messenger.server;

import net.jmb19905.messenger.crypto.EncryptedConnection;

/**
 * Represents the Connection to a Client with the username the EncryptedConnection and a boolean that represents if the Client is logged in
 */
public class ClientConnection {

    private String username = null;
    private boolean isLoggedIn;
    private final EncryptedConnection encryptedConnection;

    public ClientConnection(EncryptedConnection encryptedConnection, boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        this.encryptedConnection = encryptedConnection;
    }

    public ClientConnection(String username, EncryptedConnection encryptedConnection, boolean isLoggedIn) {
        this.username = username;
        this.isLoggedIn = isLoggedIn;
        this.encryptedConnection = encryptedConnection;
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

    public EncryptedConnection getNode() {
        return encryptedConnection;
    }

    @Override
    public String toString() {
        return "ClientConnection{" +
                "username='" + username + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                ", encryptedConnection=" + encryptedConnection +
                '}';
    }
}
