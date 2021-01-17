package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import javax.swing.*;

public class DataMessage extends EMMessage{

    public String username;

    public String encryptedMessage;

    public DataMessage(){}

    @Override
    public void handleOnClient(Connection connection) {
        String sender = Util.decryptString(MessagingClient.thisDevice, username);
        ChatHistory chatHistory = MessagingClient.otherUsers.get(sender);
        if(chatHistory.getNode().getSharedSecret() != null){
            String partiallyDecryptedMessage = Util.decryptString(MessagingClient.thisDevice, encryptedMessage);
            String message = Util.decryptString(chatHistory.getNode(), partiallyDecryptedMessage);
            EncryptedMessenger.window.appendLine("<" + sender + "> " + message);
            Util.displayNotification("Message from " + sender, message, Util.getImageResource("icon.png"));
            chatHistory.addMessage(sender, message);
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
            String decryptedMessage = Util.decryptString(senderNode, encryptedMessage);

            for(Connection recipientConnection : MessagingServer.clientConnectionKeys.keySet()){
                if(MessagingServer.clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)){
                    if(MessagingServer.clientConnectionKeys.get(recipientConnection).isLoggedIn()){
                        Node recipientNode = MessagingServer.clientConnectionKeys.get(recipientConnection).getNode();
                        if(recipientNode != null) {
                            username = Util.encryptString(recipientNode, sender);
                            encryptedMessage = Util.encryptString(recipientNode, decryptedMessage);
                            recipientConnection.sendTCP(this);
                            EMLogger.trace("MessagingServer", "Passed Data from " + sender + " to " + recipient);
                            return;
                        }
                    }
                }
            }
            EMLogger.info("MessagingServer","Recipient: " + recipient + " for data from " + sender + " is offline cannot send data");
            //TODO: add to a queue
        }
    }
}
