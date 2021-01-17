package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.Util;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;

/**
 * Sent to the server when a User wants to login
 */
public class LoginMessage extends EMMessage {

    public String username;
    public String password;

    public LoginMessage() {
    }

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("LoginMessage received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node clientConnection = MessagingServer.clientConnectionKeys.get(connection).getNode();
        String username = Util.decryptString(clientConnection, this.username);
        String password = Util.decryptString(clientConnection, this.password);

        SQLiteManager.UserData userData = SQLiteManager.getUserByName(username);
        if (userData == null) {
            LoginFailedMessage fail = new LoginFailedMessage();
            fail.cause = "name";
            connection.sendTCP(fail);
        } else {
            if (BCrypt.hashpw(password, userData.salt).equals(userData.password)) {
                MessagingServer.clientConnectionKeys.get(connection).setUsername(userData.username);
                MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                connection.sendTCP(new LoginSuccessMessage());
                HashMap<EMMessage, Object[]> messageQueue = MessagingServer.messagesQueue.get(userData.username);
                if (messageQueue != null) {
                    for (EMMessage message : messageQueue.keySet()) {
                        if (message instanceof IQueueable) {
                            ((IQueueable) message).handleOnQueue(connection, messageQueue.get(message));
                        }
                    }
                }
            } else {
                LoginFailedMessage fail = new LoginFailedMessage();
                fail.cause = "pw";
                connection.sendTCP(fail);
            }
        }
    }
}
