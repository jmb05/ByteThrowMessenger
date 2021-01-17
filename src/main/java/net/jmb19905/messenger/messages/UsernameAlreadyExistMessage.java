package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;

/**
 * Tells the Client that the username he tried to register was already taken
 */
public class UsernameAlreadyExistMessage extends EMMessage {
    @Override
    public void handleOnClient(Connection connection) {
        EncryptedMessenger.setUserData("", "");
        EncryptedMessenger.setLoggedIn(false);
        EncryptedMessenger.messagingClient.register();
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("UsernameAlreadyExistMessage received on server");
    }
}
