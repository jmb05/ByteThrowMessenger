package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;

import javax.swing.*;

/**
 * Tells the Client that the Login attempt failed because the account is not registered
 */
public class NotRegisteredMessage extends EMMessage {

    public NotRegisteredMessage() {
    }

    @Override
    public void handleOnClient(Connection connection) {
        int jop = JOptionPane.showConfirmDialog(null, "Login failed. If you have no account you have to register.\nDo you want to register?", "Login failed", JOptionPane.YES_NO_CANCEL_OPTION);
        if (jop == JOptionPane.YES_OPTION) {
            EncryptedMessenger.messagingClient.register();
        } else if (jop == JOptionPane.NO_OPTION) {
            EncryptedMessenger.messagingClient.login();
        } else {
            EncryptedMessenger.messagingClient.stop(0);
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("NotRegisteredMessage received on server");
    }
}
