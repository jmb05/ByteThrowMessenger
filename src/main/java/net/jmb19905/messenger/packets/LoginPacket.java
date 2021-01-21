package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerUtils;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.EncryptionUtility;
import net.jmb19905.messenger.util.logging.BTMLogger;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;

/**
 * Sent to the server when a User wants to login
 */
public class LoginPacket extends BTMPacket {

    public String username;
    public String password;

    public LoginPacket(){}

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("LoginPacket received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        EncryptedConnection clientConnectionEncryptedConnection = MessagingServer.clientConnectionKeys.get(connection).getEncryptedConnection();
        String username = EncryptionUtility.decryptString(clientConnectionEncryptedConnection, this.username);
        String password = EncryptionUtility.decryptString(clientConnectionEncryptedConnection, this.password);

        SQLiteManager.UserData userData = SQLiteManager.getUserByName(username);
        if (userData == null) {
            connection.sendTCP(ServerUtils.createLoginNameFailPacket());
        } else {
            if (BCrypt.hashpw(password, userData.salt).equals(userData.password)) {
                MessagingServer.clientConnectionKeys.get(connection).setUsername(userData.username);
                MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                BTMLogger.info("MessagingServer", "User logged in: " + userData.username);
                connection.sendTCP(ServerUtils.createLoginSuccessPacket());
                HashMap<BTMPacket, Object[]> messageQueue = MessagingServer.messagesQueue.get(userData.username);
                if (messageQueue != null) {
                    for (BTMPacket message : messageQueue.keySet()) {
                        if (message instanceof IQueueable) {
                            ((IQueueable) message).handleOnQueue(connection, messageQueue.get(message));
                        }
                    }
                }
                MessagingServer.messagesQueue.remove(userData.username);
            } else {
                connection.sendTCP(ServerUtils.createLoginPasswordFailPacket());
            }
        }
    }
}
