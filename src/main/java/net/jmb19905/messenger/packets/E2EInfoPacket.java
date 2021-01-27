package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.client.UserConnection;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.ClientConnection;
import net.jmb19905.messenger.server.E2EConnection;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.util.EncryptionUtility;
import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.swing.*;
import java.util.HashMap;

public class E2EInfoPacket extends BTMPacket implements IQueueable{

    public String username;
    public String type;

    public E2EInfoPacket(){}

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        String sender = EncryptionUtility.decryptString(MessagingClient.serverConnection, username);
        UserConnection connectionToSender = MessagingClient.otherUsers.get(sender);
        if(sender == null || connectionToSender == null){
            return;
        }
        String decryptedType = EncryptionUtility.decryptString(MessagingClient.serverConnection,  type);
        if(decryptedType.equals("close")){
            BTMLogger.info("MessagingClient", sender + " has closed the connection");
            JOptionPane.showMessageDialog(ByteThrowClient.window, sender + " has closed the connection");
            ByteThrowClient.window.removeConnectedUser(sender);
            MessagingClient.otherUsers.get(sender).close();
            MessagingClient.otherUsers.remove(sender);
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        ClientConnection senderConnection = MessagingServer.clientConnectionKeys.get(connection);
        if(!senderConnection.isLoggedIn()){
            return;
        }
        String sender = senderConnection.getUsername();
        String recipient = EncryptionUtility.decryptString(senderConnection.getEncryptedConnection(), username);
        String decryptedType = EncryptionUtility.decryptString(senderConnection.getEncryptedConnection(), type);

        if(decryptedType.equals("close")) {
            E2EConnection e2EConnection = new E2EConnection(sender, recipient);
            MessagingServer.e2eConnectedClients.removeIf(e2e -> e2e.equals(e2EConnection));
        }
        MessagingServer.writeE2EConnectionsToFile();

        Object[] extraData = new Object[]{sender, decryptedType};

        for (Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()) {
            if (MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)) {
                if (MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()) {
                    handleOnQueue(recipientConnection, extraData);
                    BTMLogger.trace("MessagingServer", "Passed Info from " + sender + " to " + recipient);
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

        BTMLogger.info("MessagingServer", "Recipient: " + recipient + " for info from " + sender + " is offline - added to Queue");

    }

    @Override
    public void handleOnQueue(Connection connection, Object[] extraData) {
        try {
            EncryptedConnection recipientConnection = MessagingServer.clientConnectionKeys.get(connection).getEncryptedConnection();
            String sender = (String) extraData[0];
            String type = (String) extraData[1];
            E2EInfoPacket infoPacket = new E2EInfoPacket();
            infoPacket.username = EncryptionUtility.encryptString(recipientConnection, sender);
            infoPacket.type = EncryptionUtility.encryptString(recipientConnection, type);
            connection.sendTCP(infoPacket);
        }catch (ArrayIndexOutOfBoundsException e){
            BTMLogger.warn("DataMessage", "Error parsing data for Message: " + this + " from queue", e);
        }
    }
}
