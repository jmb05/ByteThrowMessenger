package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.ClientConnection;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerUtils;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.EncryptionUtility;
import org.mindrot.jbcrypt.BCrypt;

public class ChangePacket extends BTMPacket{

    public String user;
    public String password;
    public String type;
    public String change;

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("ChangePacket received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        ClientConnection clientConnection = MessagingServer.clientConnectionKeys.get(connection);
        if(clientConnection == null) { return; }
        String decryptedUser = EncryptionUtility.decryptString(clientConnection.getEncryptedConnection(), user);
        String decryptedPassword = EncryptionUtility.decryptString(clientConnection.getEncryptedConnection(), password);
        String decryptedType = EncryptionUtility.decryptString(clientConnection.getEncryptedConnection(), type);
        String decryptedChange = EncryptionUtility.decryptString(clientConnection.getEncryptedConnection(), change);

        if(!clientConnection.getUsername().equals(decryptedUser)){
            MessagingServer.watchList.add(clientConnection.getUsername());
            MessagingServer.saveWatchlist();
            connection.sendTCP(ServerUtils.createChangeNameWrongCredentialsPacket());
            return;
        }

        SQLiteManager.UserData userData = SQLiteManager.getUserByName(decryptedUser);

        if(!BCrypt.hashpw(decryptedPassword, userData.salt).equals(userData.password)){
            connection.sendTCP(ServerUtils.createChangeNameWrongCredentialsPacket());
            return;
        }

        if(decryptedType.equals("name")){
            if(SQLiteManager.getUserByName(decryptedChange) != null){
                connection.sendTCP(ServerUtils.createRegisterNameTakenPacket("change"));
                return;
            }
            if(!SQLiteManager.changeUserName(decryptedUser, decryptedChange)) {
                connection.sendTCP(ServerUtils.createInternalErrorPacket("change"));
            }else{
                connection.sendTCP(ServerUtils.createUsernameChangeSuccessPacket(decryptedChange));
            }
        }else if(decryptedType.equals("pw")){
            if(!SQLiteManager.changeUserPassword(decryptedUser, decryptedChange)) {
                connection.sendTCP(ServerUtils.createInternalErrorPacket("change"));
            }else{
                connection.sendTCP(ServerUtils.createPasswordChangeSuccessPacket(decryptedChange));
            }
        }
    }
}
