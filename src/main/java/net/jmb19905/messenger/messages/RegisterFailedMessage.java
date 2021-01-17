package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;

import javax.swing.*;

public class RegisterFailedMessage extends EMMessage{

    public String cause;

    public RegisterFailedMessage(){}


    @Override
    public void handleOnClient(Connection connection){
        JOptionPane.showMessageDialog(null, "Server Error registering user. " + cause + ". Please again try later.", "Error registering", JOptionPane.ERROR_MESSAGE);
        EncryptedMessenger.wipeUserData();
        EncryptedMessenger.messagingClient.login(connection);
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("RegisterFailedMessage received on server");
    }
}
