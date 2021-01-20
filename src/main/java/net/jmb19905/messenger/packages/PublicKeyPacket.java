package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.server.ByteThrowServer;
import net.jmb19905.messenger.server.ClientConnection;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerUtils;
import net.jmb19905.messenger.util.logging.BTMLogger;
import net.jmb19905.messenger.util.EncryptionUtility;

import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Sent on started connection between Server and Client to ensure an encrypted connection
 */
public class PublicKeyPacket extends BTMPacket {

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
            connection.sendTCP(ServerUtils.createClientOutOfDatePacket());
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
        EncryptedConnection clientConnection = initNode(connection, encodedKey);
        assert clientConnection != null : "Error initializing Client EncryptedConnection";
        PublicKeyPacket publicKeyPacket = new PublicKeyPacket();
        publicKeyPacket.encodedKey = clientConnection.getPublicKey().getEncoded();
        connection.sendTCP(publicKeyPacket);
        BTMLogger.trace("MessagingServer", "Sent Public Key");
    }

    /**
     * Initializes a EncryptedConnection when the PublicKey of a Client is received and adds is to the clientConnections
     * @param connection the connection to the Client
     * @param encodedKey the PublicKey from the Client encoded as byte-array
     * @return the EncryptedConnection
     */
    private EncryptedConnection initNode(Connection connection, byte[] encodedKey) {
        try {
            EncryptedConnection clientConnection = new EncryptedConnection();
            PublicKey publicKey = EncryptionUtility.createPublicKeyFromData(encodedKey);
            clientConnection.setReceiverPublicKey(publicKey);
            MessagingServer.clientConnectionKeys.put(connection, new ClientConnection(clientConnection, false));
            return clientConnection;
        } catch (InvalidKeySpecException e) {
            BTMLogger.warn("MessagingServer", "Error initializing EncryptedConnection. Key is invalid.");
            return null;
        }
    }

}
