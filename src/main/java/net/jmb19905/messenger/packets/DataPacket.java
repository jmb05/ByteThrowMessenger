package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.client.UserConnection;
import net.jmb19905.messenger.client.ui.conversation.ConversationPane;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.messages.ImageMessage;
import net.jmb19905.messenger.messages.TextMessage;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerUtils;
import net.jmb19905.messenger.util.*;
import net.jmb19905.messenger.util.logging.BTMLogger;

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
public class DataPacket extends BTMPacket implements IQueueable {

    public String username;
    public String type;
    public byte[][] data;

    public DataPacket(){}

    @Override
    public void handleOnClient(Connection connection) {
        try {
            String sender = EncryptionUtility.decryptString(MessagingClient.serverConnection, username);
            UserConnection userConnection = MessagingClient.otherUsers.get(sender);
            if (userConnection.getEncryptedConnection().getSharedSecret() != null) {
                decodePackage(sender, userConnection);
            } else {
                BTMLogger.warn("MessagingClient", "Received Message from unconnected client");
            }
        } catch (NullPointerException e) {
            BTMLogger.warn("MessagingClient", "Received invalid message", e);
        }
    }

    private void decodePackage(String sender, UserConnection userConnection){
        byte[][] partiallyDecryptedData = EncryptionUtility.decrypt2DBytes(MessagingClient.serverConnection, data);
        byte[][] byteData = EncryptionUtility.decrypt2DBytes(userConnection.getEncryptedConnection(), partiallyDecryptedData);
        String partiallyType = EncryptionUtility.decryptString(MessagingClient.serverConnection, type);
        String decryptedType = EncryptionUtility.decryptString(userConnection.getEncryptedConnection(), partiallyType);
        if(decryptedType.equals("text")) {
            decodeTextPackage(sender, userConnection, byteData[0]);
        }else if(decryptedType.equals("image")){
            try {
                decodeImages(sender, userConnection, byteData);
            } catch (IOException e) {
                BTMLogger.warn("MessagingClient", "Error decoding image");
            }
        }
    }

    private void decodeTextPackage(String sender, UserConnection userConnection, byte[] byteDatum) {
        String message = new String(byteDatum, StandardCharsets.UTF_8);
        TextMessage textMessage = new TextMessage(sender, message);
        ByteThrowClient.window.addMessage(textMessage, ConversationPane.LEFT);
        Util.displayNotification("Message from " + sender, message, FileUtility.getImageResource("icon.png"));
        userConnection.addMessage(textMessage);
    }

    private void decodeImages(String sender, UserConnection userConnection, byte[][] byteData) throws IOException {
        String caption = new String(byteData[0], StandardCharsets.UTF_8);
        String lastNameMeta = "";
        String lastFormatMeta = "";
        int lastWidthMeta = 0;
        int lastHeightMeta = 0;
        FormattedImage[] images = new FormattedImage[(byteData.length - 1)/2];
        for(int i = 1; i< byteData[i].length; i++) {
            if(i % 2 == 1) {
                String metaData = new String(byteData[i], StandardCharsets.UTF_8);
                String[] parts = metaData.split("\\|");
                lastNameMeta = parts[0];
                lastFormatMeta = parts[1];
                lastWidthMeta = Integer.parseInt(parts[2]);
                lastHeightMeta = Integer.parseInt(parts[3]);
            }else if(i % 2 == 0){
                initImage(byteData[i], lastNameMeta, lastFormatMeta, lastWidthMeta, lastHeightMeta, images, i);
            }
        }
        ImageMessage imageMessage = new ImageMessage(sender, caption, images);
        ByteThrowClient.window.addMessage(imageMessage, ConversationPane.LEFT);
        Util.displayNotification("Message from " + sender, caption + "\n(+ " + images.length + " Images)", FileUtility.getImageResource("icon.png"));
        userConnection.addMessage(imageMessage);
    }

    private void initImage(byte[] byteData, String name, String format, int width, int height, FormattedImage[] images, int index) throws IOException {
        BufferedImage image = FileUtility.convertBytesToImage(byteData);
        if(width != image.getWidth() || height != image.getHeight()){
            BTMLogger.warn("MessagingClient", "The decoded image doesn't match with the meta-data");
        }else {
            int nameIndex = 0;
            String currentTime = new SimpleDateFormat("yyyy-MM.dd-HH-mm-ss").format(new Date());
            String fileName = username + "-" + currentTime + "_" + nameIndex;
            File outputFile = new File("userdata/" + ByteThrowClient.getUsername() + "/media/" + fileName + "." + format);
            while (outputFile.exists()){
                nameIndex++;
                fileName = username + "-" + currentTime + "_" + nameIndex;
                outputFile = new File("userdata/" + ByteThrowClient.getUsername() + "/media/" + fileName + "." + format);
            }
            FormattedImage formattedImage = new FormattedImage(fileName, format, image);
            formattedImage.writeWithNewName(outputFile, true);
            images[index] = formattedImage;
        }
    }

    @Override
    public void handleOnServer(Connection connection) {
        EncryptedConnection senderEncryptedConnection = MessagingServer.clientConnectionKeys.get(connection).getEncryptedConnection();
        if (MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()) {
            String sender = MessagingServer.clientConnectionKeys.get(connection).getUsername();
            String recipient = EncryptionUtility.decryptString(senderEncryptedConnection, username);
            byte[][] decryptedData = EncryptionUtility.decrypt2DBytes(senderEncryptedConnection, data);
            String decryptedType = EncryptionUtility.decryptString(senderEncryptedConnection, type);

            Object[] extraData = new Object[]{sender, decryptedType, decryptedData};

            for (Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()) {
                if (MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)) {
                    if (MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()) {
                        handleOnQueue(recipientConnection, extraData);
                        BTMLogger.trace("MessagingServer", "Passed Data from " + sender + " to " + recipient);
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
            queueData.put(this, extraData);
            MessagingServer.messagesQueue.put(recipient, queueData);

            BTMLogger.info("MessagingServer", "Recipient: " + recipient + " for message from " + sender + " is offline - added to Queue");
        }
    }

    @Override
    public void handleOnQueue(Connection connection, Object[] extraData) {
        try {
            EncryptedConnection recipientEncryptedConnection = MessagingServer.clientConnectionKeys.get(connection).getEncryptedConnection();
            String decryptedUserName = (String) extraData[0];
            String decryptedType = (String) extraData[1];
            byte[][] decryptedData = (byte[][]) extraData[2];
            DataPacket dataPacket = new DataPacket();
            dataPacket.username = EncryptionUtility.encryptString(recipientEncryptedConnection, decryptedUserName);
            dataPacket.type = EncryptionUtility.encryptString(recipientEncryptedConnection, decryptedType);
            dataPacket.data = EncryptionUtility.encrypt2DBytes(recipientEncryptedConnection, decryptedData);
            connection.sendTCP(dataPacket);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            BTMLogger.warn("DataMessage", "Error parsing data for Message: " + this + " from queue", e);
        }
    }
}
