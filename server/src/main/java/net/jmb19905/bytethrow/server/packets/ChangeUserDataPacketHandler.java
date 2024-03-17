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

package net.jmb19905.bytethrow.server.packets;

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.packets.ChangeUserDataPacket;
import net.jmb19905.bytethrow.common.packets.ChatsPacket;
import net.jmb19905.bytethrow.common.packets.SuccessPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.net.event.ContextFuture;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;

import java.net.SocketAddress;
import java.util.Optional;

public class ChangeUserDataPacketHandler implements PacketHandler<ChangeUserDataPacket> {

    @Override
    public void handle(HandlingContext ctx, ChangeUserDataPacket packet) {
        SocketAddress address = ctx.getRemote();
        ServerManager manager = StartServer.manager;
        User user = manager.getClient(address);
        String oldName = user.getUsername();
        switch (packet.type) {
            case "username" -> {
                String newUsername = packet.value;
                if (DatabaseManager.changeUsername(oldName, newUsername)) {
                    manager.changeName(user, newUsername);
                    ContextFuture<HandlingContext> channelFuture = sendUsernameSuccessPacket(ctx);
                    channelFuture.addListener(l -> {
                        ChatsPacket chatsPacket = new ChatsPacket();
                        chatsPacket.update = true;

                        manager.getOnlineClients().keySet().forEach(client -> {
                            manager.getChats(client).forEach(chat -> chatsPacket.chatData.add(new ChatsPacket.ChatData(chat)));

                            Optional<SocketAddress> optionalAddress = manager.getNetThread().getConnectedClients().keySet().stream()
                                    .filter(sAddress -> manager.getClient(sAddress).equals(client))
                                    .findFirst();
                            if (optionalAddress.isPresent()) {
                                SocketAddress peerAddress = optionalAddress.get();
                                Logger.trace("Sending packet " + packet + " to " + peerAddress);
                                net.jmb19905.net.NetworkingUtility.send(manager.getNetThread(), peerAddress, packet);
                            }
                        });
                    });
                } else {
                    NetworkingUtility.sendFail(ctx, "change_username", "error_change_username", newUsername);
                }
            }
            case "password" -> {
                if (DatabaseManager.changePassword(oldName, packet.value)) {
                    sendPasswordSuccessPacket(ctx);
                } else {
                    NetworkingUtility.sendFail(ctx, "change_password", "error_change_pw", "");
                }
            }
            case "delete" -> {
                if (DatabaseManager.deleteUser(oldName)) {
                    sendDeleteSuccessPacket(ctx);
                    //TODO: close connection
                    //connection.markClosed();
                } else {
                    NetworkingUtility.sendFail(ctx, "change_password", "error_change_pw", "");
                }
            }
        }
    }

    private ContextFuture<HandlingContext> sendUsernameSuccessPacket(HandlingContext ctx) {
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = SuccessPacket.SuccessType.CHANGE_NAME;

        return NetworkingUtility.sendPacket(successPacket, ctx);
    }

    private void sendPasswordSuccessPacket(HandlingContext ctx) {
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = SuccessPacket.SuccessType.CHANGE_PW;

        NetworkingUtility.sendPacket(successPacket, ctx);
    }

    private void sendDeleteSuccessPacket(HandlingContext ctx) {
        SuccessPacket successPacket = new SuccessPacket();
        successPacket.type = SuccessPacket.SuccessType.DELETE;

        NetworkingUtility.sendPacket(successPacket, ctx);
    }
}
