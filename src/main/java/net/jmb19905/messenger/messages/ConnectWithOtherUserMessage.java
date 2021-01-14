package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.util.EMLogger;

public class ConnectWithOtherUserMessage extends EMMessage{

    public String username;
    public String publicKeyEncodedEncrypted;

    public ConnectWithOtherUserMessage(){}

    @Override
    public void handleOnClient(Connection connection) {
        EMLogger.trace("MessagingClient", "Received ConnectWithOtherUserMessage");
        if(MessagingClient.otherUsers.get(username) != null){
            EMLogger.trace("MessagingClient", username + " is already connected");
        }else{
            Node node = new Node();
            EncryptedMessenger.messagingClient.setPublicKey(publicKeyEncodedEncrypted, node);
            MessagingClient.otherUsers.put(username, node);
            EMLogger.trace("MessagingClient", "Added " + username + " to connected users");
            if(MessagingClient.connectionRequested.contains(username)){
                EMLogger.info("MessagingClient", username + " has responded");
                MessagingClient.connectionRequested.remove(username);
                ConnectionVerificationMessage message = new ConnectionVerificationMessage();
                message.username = username;
                message.message = MessagingClient.otherUsers.get(username).encrypt(ConnectionVerificationMessage.defaultMessage);
                EncryptedMessenger.messagingClient.client.sendTCP(message);
            }else{
                ConnectWithOtherUserMessage message = new ConnectWithOtherUserMessage();
                username = MessagingClient.thisDevice.encrypt(username);
                message.publicKeyEncodedEncrypted = MessagingClient.thisDevice.encrypt(new String(MessagingClient.thisDevice.getPublicKey().getEncoded()));
                EncryptedMessenger.messagingClient.client.sendTCP(message);
            }
        }
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node senderNode = ServerMain.messagingServer.clientConnectionKeys.get(connection).getNode();
        if(ServerMain.messagingServer.clientConnectionKeys.get(connection).isLoggedIn()){
            String sender = ServerMain.messagingServer.clientConnectionKeys.get(connection).getUsername();
            String recipient = senderNode.decrypt(username);
            String publicKeyEncoded = senderNode.decrypt(publicKeyEncodedEncrypted);
            for(Connection recipientConnection : ServerMain.messagingServer.clientConnectionKeys.keySet()){
                if(ServerMain.messagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()){
                    if(ServerMain.messagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)){
                        username = ServerMain.messagingServer.clientConnectionKeys.get(recipientConnection).getNode().encrypt(sender);
                        publicKeyEncodedEncrypted = ServerMain.messagingServer.clientConnectionKeys.get(recipientConnection).getNode().encrypt(publicKeyEncoded);
                        recipientConnection.sendTCP(this);
                        EMLogger.trace("MessagingServer","Passed Connection Request from " + sender + " to " + recipient);
                        return;
                    }
                }
            }
            EMLogger.info("MessagingServer","Recipient: " + recipient + " for connection request from " + sender + " is offline cannot send request");
            //TODO: add to a queue
        }
    }
}
