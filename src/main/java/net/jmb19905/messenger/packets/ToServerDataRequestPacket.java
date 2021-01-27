package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.ClientConnection;
import net.jmb19905.messenger.server.E2EConnection;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerUtils;
import net.jmb19905.messenger.util.EncryptionUtility;

public class ToServerDataRequestPacket extends BTMPacket{

    public String type;
    public String data;

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("ToServerDataRequestPacket received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        ClientConnection clientConnection = MessagingServer.clientConnectionKeys.get(connection);
        if(type.equals("chatHistory")){
            String sender = clientConnection.getUsername();
            String otherUser = EncryptionUtility.decryptString(clientConnection.getEncryptedConnection(), data);
            E2EConnection e2EConnection = new E2EConnection(sender, otherUser);
            for(E2EConnection e2e : MessagingServer.e2eConnectedClients){
                if(e2e.namesMatch(e2EConnection)){
                    e2EConnection = e2e;
                    break;
                }
            }
            connection.sendTCP(ServerUtils.createHistoryPacket(clientConnection.getEncryptedConnection(), e2EConnection, otherUser));
        }
    }
}
