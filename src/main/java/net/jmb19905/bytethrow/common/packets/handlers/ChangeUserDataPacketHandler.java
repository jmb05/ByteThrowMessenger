/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.common.packets.ChangeUserDataPacket;
import net.jmb19905.bytethrow.common.packets.ChatsPacket;
import net.jmb19905.bytethrow.common.packets.SuccessPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

import java.util.Optional;

public class ChangeUserDataPacketHandler extends PacketHandler {

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) throws IllegalSideException {
        throw new IllegalSideException("ChangeUserDataPacket received on Client");
    }

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler serverHandler) {
        ChangeUserDataPacket changeUserDataPacket = (ChangeUserDataPacket) packet;
        TcpServerConnection serverConnection = (TcpServerConnection) serverHandler.getConnection();
        Encryption encryption = serverHandler.getEncryption();
        ServerManager manager = StartServer.manager;
        String oldName = manager.getClientName(serverHandler);
        switch (changeUserDataPacket.type) {
            case "username":
                String newUsername = changeUserDataPacket.value;
                if (DatabaseManager.changeUsername(oldName, newUsername)) {
                    manager.removeOnlineClient(oldName);
                    manager.addOnlineClient(newUsername, serverHandler);
                    manager.changeName(oldName, newUsername);
                    ChannelFuture channelFuture = sendUsernameSuccessPacket(ctx.channel(), encryption);
                    channelFuture.addListener(l -> {
                        ChatsPacket chatsPacket = new ChatsPacket();
                        chatsPacket.update = true;

                        manager.getOnlineClients().keySet().forEach(client -> {
                            manager.getChats(client).forEach(chat -> chatsPacket.chatData.add(new ChatsPacket.ChatData(chat)));

                            Optional<TcpServerHandler> optionalServerHandler = serverConnection.getClientConnections().keySet().stream().filter(handler -> manager.getClientName(handler).equals(client)).findFirst();
                            if (optionalServerHandler.isPresent()) {
                                TcpServerHandler peerHandler = optionalServerHandler.get();
                                SocketChannel peerChannel = serverConnection.getClientConnections().get(peerHandler);

                                Logger.trace("Sending packet " + packet + " to " + peerChannel.remoteAddress());
                                NetworkingUtility.sendPacket(packet, peerChannel, peerHandler.getEncryption());
                            }
                        });
                    });
                } else {
                    NetworkingUtility.sendFail(ctx.channel(), "change_username", "error_change_username", newUsername, serverHandler);
                }
                break;
            case "password":
                if (DatabaseManager.changePassword(oldName, changeUserDataPacket.value)) {
                    sendPasswordSuccessPacket(ctx.channel(), encryption);
                } else {
                    NetworkingUtility.sendFail(ctx.channel(), "change_password", "error_change_pw", "", serverHandler);
                }
                break;
            case "delete":
                if (DatabaseManager.deleteUser(oldName)) {
                    sendDeleteSuccessPacket(ctx.channel(), encryption);
                    serverConnection.markClosed();
                } else {
                    NetworkingUtility.sendFail(ctx.channel(), "change_password", "error_change_pw", "", serverHandler);
                }
                break;
        }
    }

    private ChannelFuture sendUsernameSuccessPacket(Channel channel, Encryption encryption) {
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = "change_username";

        return NetworkingUtility.sendPacket(successPacket, channel, encryption);
    }

    private void sendPasswordSuccessPacket(Channel channel, Encryption encryption) {
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = "change_password";

        NetworkingUtility.sendPacket(successPacket, channel, encryption);
    }

    private void sendDeleteSuccessPacket(Channel channel, Encryption encryption) {
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = "delete";

        NetworkingUtility.sendPacket(successPacket, channel, encryption);
    }
}
