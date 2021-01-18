package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Transfers text between two users
 */
public class DataPackage extends EMPackage implements IQueueable {

    public String username;
    public String type;
    public byte[] encryptedMessage;

    @Override
    public void handleOnClient(Connection connection) {
        try {
            String sender = Util.decryptString(MessagingClient.thisDevice, username);
            ChatHistory chatHistory = MessagingClient.otherUsers.get(sender);
            if (chatHistory.getNode().getSharedSecret() != null) {
                byte[] partiallyDecryptedMessage = MessagingClient.thisDevice.decrypt(encryptedMessage);
                byte[] byteMessage = chatHistory.getNode().decrypt(partiallyDecryptedMessage);
                String partiallyType = Util.decryptString(MessagingClient.thisDevice, type);
                String decryptedType = Util.decryptString(chatHistory.getNode(), partiallyType);
                if(decryptedType.equals("text")) {
                    String message = new String(byteMessage, StandardCharsets.UTF_8);
                    EncryptedMessenger.window.appendLine("<" + sender + "> " + message);
                    Util.displayNotification("Message from " + sender, message, Util.getImageResource("icon.png"));
                    chatHistory.addMessage(sender, message);
                }else if(decryptedType.equals("image")){
                    BufferedImage image = Util.convertBytesToImage(byteMessage);
                    File outputFile = new File("userdata/" + EncryptedMessenger.getUsername() + "/media/image-" + new SimpleDateFormat("yyyy-MM.dd-HH-mm-ss").format(new Date()) + ".png");
                    Util.createFile(outputFile);
                    ImageIO.write(image, "png", outputFile);
                }
            } else {
                EMLogger.warn("MessagingClient", "Received Message from unconnected client");
            }
        } catch (NullPointerException e) {
            EMLogger.warn("MessagingClient", "Received invalid message", e);
        } catch (IOException e) {
            EMLogger.warn("MessagingClient", " Error parsing image from DataPackage", e);
        }
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node senderNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
        if (MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()) {
            String sender = MessagingServer.clientConnectionKeys.get(connection).getUsername();
            String recipient = Util.decryptString(senderNode, username);
            byte[] decryptedMessage = senderNode.decrypt(encryptedMessage);
            String decryptedType = Util.decryptString(senderNode, type);

            Object[] extraData = new Object[]{sender, decryptedType, decryptedMessage};

            for (Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()) {
                if (MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)) {
                    if (MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()) {
                        handleOnQueue(recipientConnection, extraData);
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
            queueData.put(this, extraData);
            MessagingServer.messagesQueue.put(recipient, queueData);

            EMLogger.info("MessagingServer", "Recipient: " + recipient + " for message from " + sender + " is offline - added to Queue");
        }
    }

    @Override
    public void handleOnQueue(Connection connection, Object[] extraData) {
        try {
            Node recipientNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
            String decryptedUserName = (String) extraData[0];
            String decryptedType = (String) extraData[1];
            byte[] decryptedMessage = (byte[]) extraData[2];
            DataPackage dataPackage = new DataPackage();
            dataPackage.username = Util.encryptString(recipientNode, decryptedUserName);
            dataPackage.type = Util.encryptString(recipientNode, decryptedType);
            dataPackage.encryptedMessage = recipientNode.encrypt(decryptedMessage);
            connection.sendTCP(dataPackage);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            EMLogger.warn("DataMessage", "Error parsing data for Message: " + this + " from queue", e);
        }
    }
}
