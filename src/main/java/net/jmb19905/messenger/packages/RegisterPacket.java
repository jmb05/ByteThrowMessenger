package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerUtils;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.logging.BTMLogger;
import net.jmb19905.messenger.util.EncryptionUtility;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

/**
 * Sent to the server when a Client wants to register a new account
 */
public class RegisterPacket extends BTMPacket {

    public String username;
    public String password;

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("RegisterPacket received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        try {
            EncryptedConnection encryptedConnection = MessagingServer.clientConnectionKeys.get(connection).getNode();
            if (!MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()) {
                String username = EncryptionUtility.decryptString(encryptedConnection, this.username);
                String password = EncryptionUtility.decryptString(encryptedConnection, this.password);
                SQLiteManager.UserData user = SQLiteManager.getUserByName(username);
                if (user == null) {
                    //User does not exist create a new one
                    connection.sendTCP(ServerUtils.createRegisterSuccessPacket());
                    MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                } else {
                    if (BCrypt.hashpw(password, user.salt).equals(user.password)) {
                        BTMLogger.trace("MessagingServer", "Client tried to register instead of login -> logging client in");
                        MessagingServer.clientConnectionKeys.get(connection).setUsername(user.username);
                        MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                        connection.sendTCP(ServerUtils.createLoginSuccessPacket());
                    } else {
                        connection.sendTCP(ServerUtils.createRegisterNameTakenPacket());
                    }
                }
            } else {
                BTMLogger.warn("MessagingServer", "Already registered client tried to register");
            }
        } catch (NullPointerException e) {
            BTMLogger.warn("MessagingServer", "Error adding user");
            connection.sendTCP(ServerUtils.createInternalRegisterFailPacket());
        }
    }
}
