package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import java.util.Arrays;
import java.util.HashMap;

public class ConnectWithOtherUserMessage extends EMMessage implements IQueueable{

    public String username;
    public byte[] publicKeyEncodedEncrypted;

    public ConnectWithOtherUserMessage(){}

    @Override
    public void handleOnClient(Connection connection) {
        EMLogger.trace("MessagingClient", "Received ConnectWithOtherUserMessage");
        String decryptedUsername = Util.decryptString(MessagingClient.thisDevice, username);
        byte[] publicKeyEncodedDecrypted = MessagingClient.thisDevice.decrypt(publicKeyEncodedEncrypted);
        if(MessagingClient.otherUsers.get(decryptedUsername) != null){
            EMLogger.trace("MessagingClient", "Changing/Adding key for" + decryptedUsername);
            Node node = MessagingClient.otherUsers.get(decryptedUsername);
            EncryptedMessenger.messagingClient.setPublicKey(publicKeyEncodedDecrypted, node);
        }else{
            Node node = new Node();
            EncryptedMessenger.messagingClient.setPublicKey(publicKeyEncodedDecrypted, node);
            MessagingClient.otherUsers.put(decryptedUsername, node);
            EMLogger.trace("MessagingClient", "Added " + decryptedUsername + " to connected users");
            if(MessagingClient.connectionRequested.contains(decryptedUsername)){
                EMLogger.info("MessagingClient", decryptedUsername + " has responded");
                MessagingClient.connectionRequested.remove(decryptedUsername);
            }else{
                username = Util.encryptString(MessagingClient.thisDevice, decryptedUsername);
                publicKeyEncodedEncrypted = MessagingClient.thisDevice.encrypt(MessagingClient.otherUsers.get(decryptedUsername).getPublicKey().getEncoded());
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
        if(sender.equals(recipient)){
            EMLogger.warn("MessagingServer", "Client " + sender + " tried to connect with himself");
            return;
        }else if(SQLiteManager.getUserByName(recipient) == null){
            EMLogger.warn("MessagingServer", "Client " + sender + " tried to connect with nonexistent user " + recipient);
            return;
        }

        for(Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()){
            if(MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()){
                if(MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)){
                    Object[] data = new Object[]{sender, publicKeyEncoded};
                    handleOnQueue(recipientConnection, data);
                    return;
                }
            }
        }

        HashMap<EMMessage, Object[]> queueData;
        if(!MessagingServer.messagesQueue.containsKey(recipient)){
            queueData = new HashMap<>();
        }else{
            queueData = MessagingServer.messagesQueue.get(recipient);
        }
        queueData.put(this, new Object[]{sender, publicKeyEncoded});
        MessagingServer.messagesQueue.put(recipient, queueData);

        EMLogger.info("MessagingServer","Recipient: " + recipient + " for connection request from " + sender + " is offline - added to Queue");
    }

    @Override
    public void handleOnQueue(Connection connection, Object[] data) {
        Node recipientNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
        username = Util.encryptString(recipientNode, (String) data[0]);
        publicKeyEncodedEncrypted = recipientNode.encrypt((byte[]) data[1]);
        connection.sendTCP(this);
        EMLogger.trace("MessagingServer","Passed Connection Request from " + data[0] + " to " + MessagingServer.clientConnectionKeys.get(connection).getUsername());
    }
}
