package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.BTMLogger;
import net.jmb19905.messenger.util.Util;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

/**
 * Sent to the server when a Client wants to register a new account
 */
public class RegisterPackage extends BTMPackage {

    public String username;
    public String password;

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("RegisterPackage received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        try {
            Node node = MessagingServer.clientConnectionKeys.get(connection).getNode();
            if (!MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()) {
                String username = Util.decryptString(node, this.username);
                String password = Util.decryptString(node, this.password);
                SQLiteManager.UserData user = SQLiteManager.getUserByName(username);
                if (user == null) {
                    //User does not exist create a new one
                    UUID uuid = SQLiteManager.createUser(username, password);
                    sendRegisterSuccess(connection);
                    MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                } else {
                    if (BCrypt.hashpw(password, user.salt).equals(user.password)) {
                        BTMLogger.trace("MessagingServer", "Client tried to register instead of login -> logging client in");
                        MessagingServer.clientConnectionKeys.get(connection).setUsername(user.username);
                        MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                        SuccessPackage success = new SuccessPackage();
                        success.type = "login";
                        connection.sendTCP(success);
                    } else {
                        FailPackage fail = new FailPackage();
                        fail.type = "usernameTaken";
                        connection.sendTCP(fail);
                    }
                }
            } else {
                BTMLogger.warn("MessagingServer", "Already registered client tried to register");
            }
        } catch (NullPointerException e) {
            BTMLogger.warn("MessagingServer", "Error adding user");
            FailPackage fail = new FailPackage();
            fail.type = "registerFail";
            fail.cause = "There was an internal database error";
            connection.sendTCP(fail);
        }
    }

    /**
     * Tells a Client that the registration succeeded
     *
     * @param connection the connection to the Client
     */
    public void sendRegisterSuccess(Connection connection) {
        SuccessPackage success = new SuccessPackage();
        success.type = "register";
        connection.sendTCP(success);
    }

}
