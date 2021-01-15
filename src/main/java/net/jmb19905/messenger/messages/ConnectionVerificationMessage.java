package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

@Deprecated
public class ConnectionVerificationMessage extends EMMessage{

    public String username;
    public String message;

    public static final String defaultMessage = "20210114-9:19";

    public ConnectionVerificationMessage(){}

    @Override
    public void handleOnClient(Connection connection) {
        if(MessagingClient.connectionToBeVerified.contains(username)){
            EMLogger.info("MessagingClient", "Connection with " + username + " verified");
            MessagingClient.connectionToBeVerified.remove(username);
            EncryptedMessenger.window.addConnectedUser(username);
        }
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node senderNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
        if(MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()){
            String sender = MessagingServer.clientConnectionKeys.get(connection).getUsername();
            String recipient = Util.decryptString(senderNode, username);
            String messageDecrypted = Util.decryptString(senderNode, message);
            for(Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()){
                if(MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()){
                    if(MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)){
                        username = Util.encryptString(MessagingServer.clientConnectionKeys.get(recipientConnection).getNode(), sender);
                        message = Util.encryptString(MessagingServer.clientConnectionKeys.get(recipientConnection).getNode(), messageDecrypted);
                        recipientConnection.sendTCP(this);
                        EMLogger.trace("MessagingServer","Passed Connection Verification from " + sender + " to " + recipient);
                        return;
                    }
                }
            }
            EMLogger.info("MessagingServer","Recipient: " + recipient + " for Connection Verification from " + sender + " is offline cannot send request");
            //TODO: add to a queue
        }
    }
}
