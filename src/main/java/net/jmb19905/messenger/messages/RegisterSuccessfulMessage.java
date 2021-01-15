package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.EMLogger;

import java.util.UUID;

public class RegisterSuccessfulMessage extends EMMessage{

    public String username;
    public String uuid;

    public RegisterSuccessfulMessage(){}

    @Override
    public void handleOnClient(Connection connection) {
        EncryptedMessenger.messagingClient.uuid = UUID.fromString(uuid);
        EncryptedMessenger.writeUserData();
        EncryptedMessenger.setLoggedIn(true);
        EMLogger.info("MessagingClient", "Registered Successful");
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("RegisterSuccessfulMessage received on server");
    }
}
