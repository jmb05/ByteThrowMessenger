package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.EMLogger;

/**
 * Tells the Client that the login attempt was successful
 */
public class LoginSuccessMessage extends EMMessage {

    public LoginSuccessMessage() {
    }

    @Override
    public void handleOnClient(Connection connection) {
        EMLogger.info("MessagingClient", "Logged in successfully");
        EncryptedMessenger.writeUserData();
        EncryptedMessenger.setLoggedIn(true);
        MessagingClient.initOtherUsers();
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("LoginSuccessMessage received on server");
    }
}
