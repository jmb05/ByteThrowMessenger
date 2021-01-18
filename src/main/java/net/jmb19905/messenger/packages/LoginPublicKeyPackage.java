package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.ByteThrowServer;
import net.jmb19905.messenger.server.ClientConnection;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.util.BTMLogger;
import net.jmb19905.messenger.util.Util;

import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Sent on started connection between Server and Client to ensure an encrypted connection
 */
public class LoginPublicKeyPackage extends BTMPackage {

    public String version;
    public byte[] encodedKey;

    @Override
    public void handleOnClient(Connection connection) {
        BTMLogger.trace("MessagingClient", "received Server login response");
        ByteThrowClient.messagingClient.setPublicKey(encodedKey);
        ByteThrowClient.messagingClient.login(connection);
    }

    @Override
    public void handleOnServer(Connection connection) {
        BTMLogger.trace("MessagingServer", "Received PublicKey");
        if(!version.equals(ByteThrowServer.version)){
            FailPackage fail = new FailPackage();
            fail.type = "outOfDate";
            fail.cause = ByteThrowServer.version;
            connection.sendTCP(fail);
            return;
        }
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
        BTMLogger.trace("MessagingServer", "Sent Public Key");
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
            BTMLogger.warn("MessagingServer", "Error initializing Node. Key is invalid.");
            return null;
        }
    }

}
