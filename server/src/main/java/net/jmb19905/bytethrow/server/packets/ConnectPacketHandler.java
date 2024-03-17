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
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.packets.ConnectPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.bytethrow.server.util.ClientDataFilesManager;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;

import java.net.SocketAddress;

public class ConnectPacketHandler implements PacketHandler<ConnectPacket> {

    @Override
    public void handle(HandlingContext ctx, ConnectPacket packet) {
        ServerManager manager = StartServer.manager;
        SocketAddress address = ctx.getRemote();
        User client = manager.getClient(address);
        if (client != null) {
            User peer = packet.user;
            if (DatabaseManager.hasUser(peer.getUsername())) {
                if (manager.isClientOnline(peer)) {
                    if (manager.getChat(peer, client) == null) {
                        handleNewChatRequestServer(packet, manager, address, client, peer);
                    } else if (manager.getChat(peer, client) != null) {
                        handleConnectToExistingChatRequestServer(packet, ctx, client, peer);
                    }
                } else if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
                    NetworkingUtility.sendFail(ctx, "connect:" + peer.getUsername(), "not_online", peer.getUsername());
                }
            } else {
                NetworkingUtility.sendFail(ctx, "connect:" + peer.getUsername(), "no_such_user", peer.getUsername());
            }
        } else {
            Logger.warn("Client is trying to communicate but isn't logged in!");
        }
    }

    private void handleNewChatRequestServer(ConnectPacket packet, ServerManager manager, SocketAddress address, User client, User peer) {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            PeerChat chat = new PeerChat(client, peer);
            chat.setActive(true);
            manager.addChat(chat);

            ClientDataFilesManager.writeChats(client);
            ClientDataFilesManager.writeChats(peer);

            packet.user = client;
            manager.sendPacketToPeer(peer, packet, address);
        } else {
            Logger.warn("What is this Client even doing with his life?");
        }
    }

    private void handleConnectToExistingChatRequestServer(ConnectPacket packet, HandlingContext ctx, User client, User peer) {
        ServerManager manager = StartServer.manager;
        SocketAddress address = ctx.getRemote();
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            NetworkingUtility.sendFail(ctx, "connect:" + peer.getUsername(), "chat_exists", peer.getUsername());
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            PeerChat chat = manager.getChat(peer, client);
            chat.setActive(true);

            packet.user = client;
            manager.sendPacketToPeer(peer, packet, address);
        } else if (packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT) {
            if (manager.isClientOnline(peer)) {
                packet.user = client;
                manager.sendPacketToPeer(peer, packet, address);
            }
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT) {
            if (manager.isClientOnline(peer)) {
                PeerChat chat = manager.getChat(peer, client);
                chat.setActive(true);

                packet.user = client;
                manager.sendPacketToPeer(peer, packet, address);
            }
        }
    }
}
