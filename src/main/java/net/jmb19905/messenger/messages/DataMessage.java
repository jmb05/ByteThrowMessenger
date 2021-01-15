package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

public class DataMessage extends EMMessage{

    public String username;

    public String encryptedMessage;

    public DataMessage(){}

    @Override
    public void handleOnClient(Connection connection) {
        String sender = Util.decryptString(MessagingClient.thisDevice, username);
        if(MessagingClient.otherUsers.get(sender).getSharedSecret() != null){
            String message = Util.decryptString(MessagingClient.thisDevice, Util.decryptString(MessagingClient.otherUsers.get(sender), encryptedMessage));
            System.out.println(sender + " : " + message);
        }else {
            EMLogger.warn("MessagingClient", "Received Message from unconnected client");
        }
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node senderNode = MessagingServer.clientConnectionKeys.get(connection).getNode();
        if(MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()){
            String sender = MessagingServer.clientConnectionKeys.get(connection).getUsername();
            String recipient = Util.decryptString(senderNode, username);

            for(Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()){
                if(MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()){
                    if(MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)){
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
