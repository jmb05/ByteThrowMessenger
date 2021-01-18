package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import java.util.HashMap;

/**
 * Transfers text between two users
 */
public class DataPackage extends EMPackage implements IQueueable {

    public String username;

    public String encryptedMessage;

    @Override
    public void handleOnClient(Connection connection) {
        try {
            String sender = Util.decryptString(MessagingClient.thisDevice, username);
            ChatHistory chatHistory = MessagingClient.otherUsers.get(sender);
            if (chatHistory.getNode().getSharedSecret() != null) {
                String partiallyDecryptedMessage = Util.decryptString(MessagingClient.thisDevice, encryptedMessage);
                String message = Util.decryptString(chatHistory.getNode(), partiallyDecryptedMessage);
                EncryptedMessenger.window.appendLine("<" + sender + "> " + message);
                Util.displayNotification("Message from " + sender, message, Util.getImageResource("icon.png"));
                chatHistory.addMessage(sender, message);
            } else {
                EMLogger.warn("MessagingClient", "Received Message from unconnected client");
            }
        } catch (NullPointerException e) {
            EMLogger.warn("MessagingClient", "Received invalid message", e);
        }
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node senderNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
        if (MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()) {
            String sender = MessagingServer.clientConnectionKeys.get(connection).getUsername();
            String recipient = Util.decryptString(senderNode, username);
            String decryptedMessage = Util.decryptString(senderNode, encryptedMessage);

            for (Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()) {
                if (MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)) {
                    if (MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()) {
                        handleOnQueue(recipientConnection, new Object[]{sender, decryptedMessage});
                        EMLogger.trace("MessagingServer", "Passed Data from " + sender + " to " + recipient);
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
            queueData.put(this, new Object[]{sender, decryptedMessage});
            MessagingServer.messagesQueue.put(recipient, queueData);

            EMLogger.info("MessagingServer", "Recipient: " + recipient + " for message from " + sender + " is offline - added to Queue");
        }
    }

    @Override
    public void handleOnQueue(Connection connection, Object[] extraData) {
        try {
            Node recipientNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
            String decryptedUserName = (String) extraData[0];
            String decryptedMessage = (String) extraData[1];
            DataPackage dataPackage = new DataPackage();
            dataPackage.username = Util.encryptString(recipientNode, decryptedUserName);
            dataPackage.encryptedMessage = Util.encryptString(recipientNode, decryptedMessage);
            connection.sendTCP(dataPackage);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            EMLogger.warn("DataMessage", "Error parsing data for Message: " + this + " from queue", e);
        }
    }
}
