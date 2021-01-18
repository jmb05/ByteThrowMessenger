package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.ClientConnection;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Sent on started connection between Server and Client to ensure an encrypted connection
 */
public class LoginPublicKeyPackage extends EMPackage {

    public byte[] encodedKey;

    @Override
    public void handleOnClient(Connection connection) {
        EMLogger.trace("MessagingClient", "received Server login response");
        EncryptedMessenger.messagingClient.setPublicKey(encodedKey);
        EncryptedMessenger.messagingClient.login(connection);
    }

    @Override
    public void handleOnServer(Connection connection) {
        EMLogger.trace("MessagingServer", "Received PublicKey");
        handlePublicKey(connection, encodedKey);
    }

    /**
     * Handles the PublicKey from the Client
     * Sends the Client a PublicKey
     * @param connection the connection to the Client
     * @param encodedKey the PublicKey encoded as byte-array
     */
    public void handlePublicKey(Connection connection, byte[] encodedKey) {
        Node clientConnection = initNode(connection, encodedKey);
        LoginPublicKeyPackage loginPublicKeyPackage = new LoginPublicKeyPackage();
        loginPublicKeyPackage.encodedKey = clientConnection.getPublicKey().getEncoded();
        connection.sendTCP(loginPublicKeyPackage);
        EMLogger.trace("MessagingServer", "Sent Public Key");
    }

    /**
     * Initializes a Node when the PublicKey of a Client is received and adds is to the clientConnections
     * @param connection the connection to the Client
     * @param encodedKey the PublicKey from the Client encoded as byte-array
     * @return the Node
     */
    private Node initNode(Connection connection, byte[] encodedKey) {
        try {
            Node clientConnection = new Node();
            PublicKey publicKey = Util.createPublicKeyFromData(encodedKey);
            clientConnection.setReceiverPublicKey(publicKey);
            MessagingServer.clientConnectionKeys.put(connection, new ClientConnection(clientConnection, false));
            return clientConnection;
        } catch (InvalidKeySpecException e) {
            EMLogger.warn("MessagingServer", "Error initializing Node. Key is invalid.");
            return null;
        }
    }

}
