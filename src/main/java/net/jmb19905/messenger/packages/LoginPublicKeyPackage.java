package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.util.EMLogger;

/**
 * Sent on started connection between Server and Client to ensure an encrypted connection
 */
public class LoginPublicKeyPackage extends EMPackage {

    public byte[] encodedKey;

    public LoginPublicKeyPackage() {
    }

    @Override
    public void handleOnClient(Connection connection) {
        EMLogger.trace("MessagingClient", "received Server login response");
        EncryptedMessenger.messagingClient.setPublicKey(encodedKey);
        EncryptedMessenger.messagingClient.login();
    }

    @Override
    public void handleOnServer(Connection connection) {
        EMLogger.trace("MessagingServer", "Received PublicKey");
        ServerMain.messagingServer.sendPublicKey(connection, encodedKey);
    }
}
