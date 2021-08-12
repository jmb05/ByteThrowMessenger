package net.jmb19905.common.packets.handlers.server;

import io.netty.channel.Channel;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.ChangeUserDataPacket;
import net.jmb19905.common.packets.SuccessPacket;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.database.UserDatabaseManager;
import net.jmb19905.server.networking.ServerHandler;

public class ChangeUserDataPacketHandler extends ServerPacketHandler<ChangeUserDataPacket>{

    private final ChangeUserDataPacket packet;

    public ChangeUserDataPacketHandler(ChangeUserDataPacket packet) {
        super(packet);
        this.packet = packet;
    }

    @Override
    public void handle(ServerHandler serverHandler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        if(packet.type.equals("username")){
            String newUsername = packet.value;
            if(UserDatabaseManager.changeUsername(connection.getName(), newUsername)){
                serverHandler.setName(newUsername);
                sendUsernameSuccessPacket(channel, connection.encryption);
            }else {
                sendFail(channel, "change_username", "error_change_username", newUsername, connection);
            }
        }else if(packet.type.equals("password")){
            if(UserDatabaseManager.changePassword(connection.getName(), packet.value)){
                sendPasswordSuccessPacket(channel, connection.encryption);
            }else {
                sendFail(channel, "change_password", "error_change_pw", "", connection);
            }
        }
    }

    private void sendUsernameSuccessPacket(Channel channel, EncryptedConnection encryption){
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = "change_username";

        NetworkingUtility.sendPacket(successPacket, channel, encryption);
    }

    private void sendPasswordSuccessPacket(Channel channel, EncryptedConnection encryption){
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = "change_password";

        NetworkingUtility.sendPacket(successPacket, channel, encryption);
    }

}
