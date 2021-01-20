package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.crypto.exception.InvalidNodeException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.logging.BTMLogger;
import net.jmb19905.messenger.util.EncryptionUtility;

import java.util.HashMap;

/**
 * Used when two Users want to connect
 */
public class StartEndToEndConnectionPacket extends BTMPacket implements IQueueable {

    public String username;
    public byte[] publicKeyEncodedEncrypted;

    @Override
    public void handleOnClient(Connection connection) {
        BTMLogger.trace("MessagingClient", "Received StartEndToEndConnectionPacket");
        String decryptedUsername = EncryptionUtility.decryptString(MessagingClient.serverConnection, username);
        byte[] publicKeyEncodedDecrypted = MessagingClient.serverConnection.decrypt(publicKeyEncodedEncrypted);
        if (MessagingClient.otherUsers.get(decryptedUsername) != null) {
            BTMLogger.trace("MessagingClient", "Changing/Adding key for" + decryptedUsername);
            EncryptedConnection oldEncryptedConnection = MessagingClient.otherUsers.get(decryptedUsername).getNode();
            byte[] publicKeyEncoded = oldEncryptedConnection.getPublicKey().getEncoded();
            byte[] privateKeyEncoded = oldEncryptedConnection.getPrivateKey().getEncoded();
            EncryptedConnection encryptedConnection = null;
            try {
                encryptedConnection = new EncryptedConnection(publicKeyEncoded, privateKeyEncoded, new byte[0]);
                MessagingClient.otherUsers.get(decryptedUsername).setNode(encryptedConnection);
            } catch (InvalidNodeException e) {
                BTMLogger.warn("MessagingClient", "Error changing key", e);
            }
            ByteThrowClient.messagingClient.setPublicKey(publicKeyEncodedDecrypted, encryptedConnection);
        } else {
            EncryptedConnection encryptedConnection = new EncryptedConnection();
            ByteThrowClient.messagingClient.setPublicKey(publicKeyEncodedDecrypted, encryptedConnection);
            ChatHistory chatHistory = new ChatHistory(decryptedUsername, encryptedConnection);
            MessagingClient.otherUsers.put(decryptedUsername, chatHistory);
            BTMLogger.trace("MessagingClient", "Added " + decryptedUsername + " to connected users");
            if (MessagingClient.connectionRequested.contains(decryptedUsername)) {
                BTMLogger.info("MessagingClient", decryptedUsername + " has responded");
                MessagingClient.connectionRequested.remove(decryptedUsername);
            } else {
                username = EncryptionUtility.encryptString(MessagingClient.serverConnection, decryptedUsername);
                publicKeyEncodedEncrypted = MessagingClient.serverConnection.encrypt(MessagingClient.otherUsers.get(decryptedUsername).getNode().getPublicKey().getEncoded());
                ByteThrowClient.messagingClient.client.sendTCP(this);
            }
        }
        ByteThrowClient.window.addConnectedUser(decryptedUsername);
    }

    @Override
    public void handleOnServer(Connection connection) {
        EncryptedConnection senderEncryptedConnection = MessagingServer.clientConnectionKeys.get(connection).getNode();
        String sender = MessagingServer.clientConnectionKeys.get(connection).getUsername();
        String recipient = EncryptionUtility.decryptString(senderEncryptedConnection, username);
        byte[] publicKeyEncoded = senderEncryptedConnection.decrypt(publicKeyEncodedEncrypted);
        try {
            if (sender.equals(recipient)) {
                BTMLogger.warn("MessagingServer", "Client " + sender + " tried to connect with himself");
                return;
            } else if (SQLiteManager.getUserByName(recipient) == null) {
                BTMLogger.warn("MessagingServer", "Client " + sender + " tried to connect with nonexistent user " + recipient);
                return;
            }

            for (Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()) {
                if (MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()) {
                    if (MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)) {
                        Object[] data = new Object[]{sender, publicKeyEncoded};
                        handleOnQueue(recipientConnection, data);
                        return;
                    }
                }
            }

            HashMap<BTMPacket, Object[]> queueData;
            if (!MessagingServer.messagesQueue.containsKey(recipient)) {
                queueData = new HashMap<>();
            } else {
                queueData = MessagingServer.messagesQueue.get(recipient);
            }
            queueData.put(this, new Object[]{sender, publicKeyEncoded});
            MessagingServer.messagesQueue.put(recipient, queueData);

            BTMLogger.info("MessagingServer", "Recipient: " + recipient + " for connection request from " + sender + " is offline - added to Queue");
        } catch (NullPointerException e) {
            BTMLogger.warn("MessagingServer", "Error connecting users");
        }
    }

    @Override
    public void handleOnQueue(Connection connection, Object[] data) {
        EncryptedConnection recipientEncryptedConnection = MessagingServer.clientConnectionKeys.get(connection).getNode();
        username = EncryptionUtility.encryptString(recipientEncryptedConnection, (String) data[0]);
        publicKeyEncodedEncrypted = recipientEncryptedConnection.encrypt((byte[]) data[1]);
        connection.sendTCP(this);
        BTMLogger.trace("MessagingServer", "Passed Connection Request from " + data[0] + " to " + MessagingServer.clientConnectionKeys.get(connection).getUsername());
    }
}