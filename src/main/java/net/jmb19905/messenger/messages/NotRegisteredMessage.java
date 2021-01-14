package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;

import javax.swing.*;

public class NotRegisteredMessage extends EMMessage{

    public NotRegisteredMessage(){}

    @Override
    public void handleOnClient(Connection connection) {
        int jop = JOptionPane.showConfirmDialog(null, "Login failed. If you have no account you have to register.\nDo you want to register?", "Login failed", JOptionPane.YES_NO_CANCEL_OPTION);
        if(jop == JOptionPane.YES_OPTION){
            EncryptedMessenger.messagingClient.register(connection);
        }else if(jop == JOptionPane.NO_OPTION){
            EncryptedMessenger.messagingClient.login(connection);
        }else{
            System.exit(0);
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException{
        throw new UnsupportedSideException("NotRegisteredMessage received on server");
    }
}
