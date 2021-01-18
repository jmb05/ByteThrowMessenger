package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.crypto.exception.InvalidNodeException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import java.util.HashMap;

/**
 * Used when two Users want to connect
 */
public class ConnectWithOtherUserPackage extends EMPackage implements IQueueable {

    public String username;
    public byte[] publicKeyEncodedEncrypted;

    public ConnectWithOtherUserPackage() {
    }

    @Override
    public void handleOnClient(Connection connection) {
        EMLogger.trace("MessagingClient", "Received ConnectWithOtherUserPackage");
        String decryptedUsername = Util.decryptString(MessagingClient.thisDevice, username);
        byte[] publicKeyEncodedDecrypted = MessagingClient.thisDevice.decrypt(publicKeyEncodedEncrypted);
        if (MessagingClient.otherUsers.get(decryptedUsername) != null) {
            EMLogger.trace("MessagingClient", "Changing/Adding key for" + decryptedUsername);
            Node oldNode = MessagingClient.otherUsers.get(decryptedUsername).getNode();
            byte[] publicKeyEncoded = oldNode.getPublicKey().getEncoded();
            byte[] privateKeyEncoded = oldNode.getPrivateKey().getEncoded();
            Node node = null;
            try {
                node = new Node(publicKeyEncoded, privateKeyEncoded, new byte[0]);
                MessagingClient.otherUsers.get(decryptedUsername).setNode(node);
            } catch (InvalidNodeException e) {
                EMLogger.warn("MessagingClient", "Error changing key", e);
            }
            EncryptedMessenger.messagingClient.setPublicKey(publicKeyEncodedDecrypted, node);
        } else {
            Node node = new Node();
            EncryptedMessenger.messagingClient.setPublicKey(publicKeyEncodedDecrypted, node);
            ChatHistory chatHistory = new ChatHistory(decryptedUsername, node);
            MessagingClient.otherUsers.put(decryptedUsername, chatHistory);
            EMLogger.trace("MessagingClient", "Added " + decryptedUsername + " to connected users");
            if (MessagingClient.connectionRequested.contains(decryptedUsername)) {
                EMLogger.info("MessagingClient", decryptedUsername + " has responded");
                MessagingClient.connectionRequested.remove(decryptedUsername);
            } else {
                username = Util.encryptString(MessagingClient.thisDevice, decryptedUsername);
                publicKeyEncodedEncrypted = MessagingClient.thisDevice.encrypt(MessagingClient.otherUsers.get(decryptedUsername).getNode().getPublicKey().getEncoded());
                EncryptedMessenger.messagingClient.client.sendTCP(this);
            }
        }
        EncryptedMessenger.window.addConnectedUser(decryptedUsername);
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node senderNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
        String sender = MessagingServer.clientConnectionKeys.get(connection).getUsername();
        String recipient = Util.decryptString(senderNode, username);
        byte[] publicKeyEncoded = senderNode.decrypt(publicKeyEncodedEncrypted);
        try {
            if (sender.equals(recipient)) {
                EMLogger.warn("MessagingServer", "Client " + sender + " tried to connect with himself");
                return;
            } else if (SQLiteManager.getUserByName(recipient) == null) {
                EMLogger.warn("MessagingServer", "Client " + sender + " tried to connect with nonexistent user " + recipient);
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

            HashMap<EMPackage, Object[]> queueData;
            if (!MessagingServer.messagesQueue.containsKey(recipient)) {
                queueData = new HashMap<>();
            } else {
                queueData = MessagingServer.messagesQueue.get(recipient);
            }
            queueData.put(this, new Object[]{sender, publicKeyEncoded});
            MessagingServer.messagesQueue.put(recipient, queueData);

            EMLogger.info("MessagingServer", "Recipient: " + recipient + " for connection request from " + sender + " is offline - added to Queue");
        } catch (NullPointerException e) {
            EMLogger.warn("MessagingServer", "Error connecting users");
        }
    }

    @Override
    public void handleOnQueue(Connection connection, Object[] data) {
        Node recipientNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
        username = Util.encryptString(recipientNode, (String) data[0]);
        publicKeyEncodedEncrypted = recipientNode.encrypt((byte[]) data[1]);
        connection.sendTCP(this);
        EMLogger.trace("MessagingServer", "Passed Connection Request from " + data[0] + " to " + MessagingServer.clientConnectionKeys.get(connection).getUsername());
    }
}
