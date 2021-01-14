package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.util.EMLogger;

public class DataMessage extends EMMessage{

    public String username;

    public String encryptedMessage;

    public DataMessage(){}

    @Override
    public void handleOnClient(Connection connection) {
        String sender = MessagingClient.thisDevice.decrypt(username);
        if(MessagingClient.otherUsers.get(sender).getSharedSecret() != null){
            String message = MessagingClient.thisDevice.decrypt(MessagingClient.otherUsers.get(sender).decrypt(encryptedMessage));
            System.out.println(sender + " : " + message);
        }else {
            EMLogger.warn("MessagingClient", "Received Message from unconnected client");
        }
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node senderNode = ServerMain.messagingServer.clientConnectionKeys.get(connection).getNode();
        if(ServerMain.messagingServer.clientConnectionKeys.get(connection).isLoggedIn()){
            String sender = ServerMain.messagingServer.clientConnectionKeys.get(connection).getUsername();
            String recipient = senderNode.decrypt(username);

            for(Connection recipientConnection : ServerMain.messagingServer.clientConnectionKeys.keySet()){
                if(ServerMain.messagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()){
                    if(ServerMain.messagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)){
                        username = sender;
                        recipientConnection.sendTCP(this);
                        EMLogger.trace("MessagingServer","Passed Data from " + sender + " to " + recipient);
                        return;
                    }
                }
            }
            EMLogger.info("MessagingServer","Recipient: " + recipient + " for data from " + sender + " is offline cannot send data");
            //TODO: add to a queue
        }
    }
}
