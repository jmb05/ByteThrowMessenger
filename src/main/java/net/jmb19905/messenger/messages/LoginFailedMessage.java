package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;

import javax.swing.*;

/**
 * Tells the Client that the login failed
 */
public class LoginFailedMessage extends EMMessage {

    public String cause;

    public LoginFailedMessage() {
    }

    @Override
    public void handleOnClient(Connection connection) {
        JOptionPane.showMessageDialog(null, "Could not log in! " + (cause.equals("pw") ? "Password " : "Username ") + "was incorrect.", "Wrong credentials", JOptionPane.ERROR_MESSAGE);
        EncryptedMessenger.wipeUserData();
        EncryptedMessenger.messagingClient.login();
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("LoginFailedMessage received on server");
    }
}
