package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerUtils;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.Util;
import net.jmb19905.messenger.util.logging.BTMLogger;
import net.jmb19905.messenger.util.EncryptionUtility;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Sent to the server when a Client wants to register a new account
 */
public class RegisterPacket extends BTMPacket {

    public String username;
    public String password;

    public RegisterPacket(){}

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("RegisterPacket received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        try {
            EncryptedConnection encryptedConnection = MessagingServer.clientConnectionKeys.get(connection).getEncryptedConnection();
            if (!MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()) {
                String username = EncryptionUtility.decryptString(encryptedConnection, this.username);
                String password = EncryptionUtility.decryptString(encryptedConnection, this.password);
                SQLiteManager.UserData user = SQLiteManager.getUserByName(username);
                if (user == null) {
                    if(Util.checkPasswordRules(password)) {
                        //User does not exist create a new one
                        SQLiteManager.createUser(username, password);
                        connection.sendTCP(ServerUtils.createRegisterSuccessPacket());
                        MessagingServer.clientConnectionKeys.get(connection).setUsername(username);
                        MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                    }
                } else {
                    if (BCrypt.hashpw(password, user.salt).equals(user.password)) {
                        BTMLogger.trace("MessagingServer", "Client tried to register instead of login -> logging client in");
                        MessagingServer.clientConnectionKeys.get(connection).setUsername(user.username);
                        MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                        connection.sendTCP(ServerUtils.createLoginSuccessPacket());
                    } else {
                        connection.sendTCP(ServerUtils.createRegisterNameTakenPacket("initial"));
                    }
                }
            } else {
                BTMLogger.warn("MessagingServer", "Already registered client tried to register");
            }
        } catch (NullPointerException e) {
            BTMLogger.warn("MessagingServer", "Error adding user", e);
            connection.sendTCP(ServerUtils.createInternalErrorPacket("register"));
        }
    }
}
