package net.jmb19905.common.packets.handlers.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.ChangeUserDataPacket;
import net.jmb19905.common.packets.ChatsPacket;
import net.jmb19905.common.packets.SuccessPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.database.UserDatabaseManager;
import net.jmb19905.server.networking.Server;
import net.jmb19905.server.networking.ServerHandler;

import java.util.Optional;

public class ChangeUserDataPacketHandler extends ServerPacketHandler<ChangeUserDataPacket>{

    private final ChangeUserDataPacket packet;

    public ChangeUserDataPacketHandler(ChangeUserDataPacket packet) {
        super(packet);
        this.packet = packet;
    }

    @Override
    public void handle(ServerHandler serverHandler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        String oldName = connection.getName();
        switch (packet.type) {
            case "username":
                String newUsername = packet.value;
                if (UserDatabaseManager.changeUsername(oldName, newUsername)) {
                    serverHandler.setName(newUsername);
                    Server.changeName(oldName, newUsername);
                    ChannelFuture channelFuture = sendUsernameSuccessPacket(channel, connection.encryption);
                    channelFuture.addListener(l -> {
                        ChatsPacket packet = new ChatsPacket();
                        packet.update = true;

                        String[] peers = Server.getPeerNames(newUsername);

                        for(String peer : peers) {
                            if(peer.equals(newUsername)){
                                packet.names = peers;
                            }else {
                                packet.names = Server.getPeerNames(peer);
                            }

                            Optional<ServerHandler> optionalServerHandler = Server.connections.keySet().stream().filter(p -> p.getConnection().getName().equals(peer)).findFirst();
                            if(optionalServerHandler.isPresent()){
                                ServerHandler peerHandler = optionalServerHandler.get();
                                SocketChannel peerChannel = Server.connections.get(peerHandler);

                                Logger.log("Sending packet " + packet + " to " + peerChannel.remoteAddress(), Logger.Level.TRACE);
                                NetworkingUtility.sendPacket(packet, peerChannel, peerHandler.getConnection().encryption);
                            }
                        }
                    });
                } else {
                    sendFail(channel, "change_username", "error_change_username", newUsername, connection);
                }
                break;
            case "password":
                if (UserDatabaseManager.changePassword(connection.getName(), packet.value)) {
                    sendPasswordSuccessPacket(channel, connection.encryption);
                } else {
                    sendFail(channel, "change_password", "error_change_pw", "", connection);
                }
                break;
            case "delete":
                if (UserDatabaseManager.deleteUser(serverHandler.getConnection().getName())) {
                    sendDeleteSuccessPacket(channel, connection.encryption);
                    serverHandler.markClosed();
                } else {
                    sendFail(channel, "change_password", "error_change_pw", "", connection);
                }
                break;
        }
    }

    private ChannelFuture sendUsernameSuccessPacket(Channel channel, EncryptedConnection encryption){
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = "change_username";

        return NetworkingUtility.sendPacket(successPacket, channel, encryption);
    }

    private void sendPasswordSuccessPacket(Channel channel, EncryptedConnection encryption){
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = "change_password";

        NetworkingUtility.sendPacket(successPacket, channel, encryption);
    }

    private void sendDeleteSuccessPacket(Channel channel, EncryptedConnection encryption){
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = "delete";

        NetworkingUtility.sendPacket(successPacket, channel, encryption);
    }

}
