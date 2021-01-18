package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.Util;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;

/**
 * Sent to the server when a User wants to login
 */
public class LoginPackage extends EMPackage {

    public String username;
    public String password;

    public LoginPackage() {
    }

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("LoginPackage received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node clientConnection = MessagingServer.clientConnectionKeys.get(connection).getNode();
        String username = Util.decryptString(clientConnection, this.username);
        String password = Util.decryptString(clientConnection, this.password);

        SQLiteManager.UserData userData = SQLiteManager.getUserByName(username);
        if (userData == null) {
            FailPackage fail = new FailPackage();
            fail.type = "loginFail";
            fail.cause = "name";
            connection.sendTCP(fail);
        } else {
            if (BCrypt.hashpw(password, userData.salt).equals(userData.password)) {
                MessagingServer.clientConnectionKeys.get(connection).setUsername(userData.username);
                MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                SuccessPackage successMessage = new SuccessPackage();
                successMessage.type = "login";
                connection.sendTCP(successMessage);
                HashMap<EMPackage, Object[]> messageQueue = MessagingServer.messagesQueue.get(userData.username);
                if (messageQueue != null) {
                    for (EMPackage message : messageQueue.keySet()) {
                        if (message instanceof IQueueable) {
                            ((IQueueable) message).handleOnQueue(connection, messageQueue.get(message));
                        }
                    }
                }
            } else {
                FailPackage fail = new FailPackage();
                fail.type = "loginFail";
                fail.cause = "pw";
                connection.sendTCP(fail);
            }
        }
    }
}
