package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import org.mindrot.jbcrypt.BCrypt;

public class LoginMessage extends EMMessage{

    public String username;
    public String password;

    public LoginMessage(){}

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException{
        throw new UnsupportedSideException("LoginMessage received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node clientConnection = ServerMain.messagingServer.clientConnectionKeys.get(connection).getNode();
        String username = clientConnection.decrypt(this.username);
        String password = clientConnection.decrypt(this.password);

        SQLiteManager.UserData userData = SQLiteManager.getUserByName(username);
        if(userData == null){
            LoginFailedMessage fail = new LoginFailedMessage();
            fail.cause = "name";
            connection.sendTCP(fail);
        }else{
            if(BCrypt.hashpw(password,userData.salt).equals(userData.password)) {
                ServerMain.messagingServer.clientConnectionKeys.get(connection).setUsername(userData.username);
                ServerMain.messagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                connection.sendTCP(new LoginSuccessMessage());
            }else{
                LoginFailedMessage fail = new LoginFailedMessage();
                fail.cause = "pw";
                connection.sendTCP(fail);
            }
        }
    }
}
