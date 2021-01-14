package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.EMLogger;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegisterMessage extends EMMessage {

    public String username;
    public String password;

    public RegisterMessage(){}

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException{
        throw new UnsupportedSideException("RegisterMessage received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        if(!ServerMain.messagingServer.clientConnectionKeys.get(connection).isLoggedIn()) {
            String username = ServerMain.messagingServer.clientConnectionKeys.get(connection).getNode().decrypt(this.username);
            String password = ServerMain.messagingServer.clientConnectionKeys.get(connection).getNode().decrypt(this.password);
            SQLiteManager.UserData user = SQLiteManager.getUserByName(username);
            if (user == null) {
                //User does not exist create a new one
                UUID uuid = ServerMain.messagingServer.createUser(username, password);
                ServerMain.messagingServer.sendRegisterSuccess(connection, username, uuid);
                ServerMain.messagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
            } else {
                if (BCrypt.hashpw(password,user.salt).equals(user.password)) {
                    EMLogger.trace("MessagingServer", "Client tried to register instead of login -> logging client in");
                    ServerMain.messagingServer.clientConnectionKeys.get(connection).setUsername(user.username);
                    ServerMain.messagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                    connection.sendTCP(new LoginSuccessMessage());
                } else {
                    connection.sendTCP(new UsernameAlreadyExistMessage());
                }
            }
        }else{
            EMLogger.warn("MessagingServer", "Already registered client tried to register");
        }
    }
}
