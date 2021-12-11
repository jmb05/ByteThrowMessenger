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
import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.client.ClientPeerChat;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.packets.ConnectPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.bytethrow.server.util.ClientDataFilesManager;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

import java.security.spec.InvalidKeySpecException;

public class ConnectPacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet) {
        ConnectPacket connectPacket = (ConnectPacket) packet;
        ServerManager manager = StartServer.manager;
        TcpServerHandler handler = ((TcpServerHandler) ctx.handler());
        User client = manager.getClient(handler);
        if (client != null) {
            User peer = connectPacket.user;
            if (DatabaseManager.hasUser(peer.getUsername())) {
                if (manager.isClientOnline(peer)) {
                    if (manager.getChat(peer, client) == null) {
                        handleNewChatRequestServer(connectPacket, manager, handler, client, peer);
                    } else if (manager.getChat(peer, client) != null) {
                        handleConnectToExistingChatRequestServer(connectPacket, ctx, client, peer);
                    }
                } else if (connectPacket.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
                    NetworkingUtility.sendFail(ctx, "connect:" + peer.getUsername(), "not_online", peer.getUsername());
                }
            } else {
                NetworkingUtility.sendFail(ctx, "connect:" + peer.getUsername(), "no_such_user", peer.getUsername());
            }
        } else {
            Logger.warn("Client is trying to communicate but isn't logged in!");
        }
    }

    private void handleNewChatRequestServer(ConnectPacket packet, ServerManager manager, TcpServerHandler handler, User client, User peer) {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            PeerChat chat = new PeerChat(client, peer);
            chat.setActive(true);
            manager.addChat(chat);

            ClientDataFilesManager.writeChats(client);
            ClientDataFilesManager.writeChats(peer);

            packet.user = client;
            manager.sendPacketToPeer(peer, packet, manager.getConnection(), handler);
        } else {
            Logger.warn("What is this Client even doing with his life?");
        }
    }

    private void handleConnectToExistingChatRequestServer(ConnectPacket packet, ChannelHandlerContext ctx, User client, User peer) {
        ServerManager manager = StartServer.manager;
        TcpServerHandler handler = (TcpServerHandler) ctx.handler();
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            NetworkingUtility.sendFail(ctx, "connect:" + peer.getUsername(), "chat_exists", peer.getUsername());
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            PeerChat chat = manager.getChat(peer, client);
            chat.setActive(true);

            packet.user = client;
            manager.sendPacketToPeer(peer, packet, manager.getConnection(), handler);
        } else if (packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT) {
            if (manager.isClientOnline(peer)) {
                packet.user = client;
                manager.sendPacketToPeer(peer, packet, manager.getConnection(), handler);
            }
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT) {
            if (manager.isClientOnline(peer)) {
                PeerChat chat = manager.getChat(peer, client);
                chat.setActive(true);

                packet.user = client;
                manager.sendPacketToPeer(peer, packet, manager.getConnection(), handler);
            }
        }
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet) {
        ConnectPacket connectPacket = (ConnectPacket) packet;
        User peer = connectPacket.user;
        byte[] encodedPeerKey = connectPacket.key;

        try {
            if (StartClient.manager.getChat(peer) == null) {
                handleNewChatRequestClient(connectPacket, ctx.channel(), ((TcpClientHandler) ctx.handler()).getEncryption(), peer, encodedPeerKey);
            } else if (StartClient.manager.getChat(peer) != null) {
                handleConnectToExistingChatRequestClient(connectPacket, ctx.channel(), ((TcpClientHandler) ctx.handler()).getEncryption(), peer, encodedPeerKey);
            }
        } catch (InvalidKeySpecException e) {
            Logger.error(e);
        }
    }

    private void handleConnectToExistingChatRequestClient(ConnectPacket packet, Channel channel, Encryption encryption, User peer, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Logger.warn("Peer tried create a Chat that already exists");
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            activateEncryption(peer, encodedPeerKey);
        } else if (packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT) {
            activateEncryption(peer, encodedPeerKey);

            Encryption chatEncryption = StartClient.manager.getChat(peer).getEncryption();

            packet.user = peer;
            packet.key = chatEncryption.getPublicKey().getEncoded();
            packet.connectType = ConnectPacket.ConnectType.REPLY_RECONNECT;

            NetworkingUtility.sendPacket(packet, channel, encryption);
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT) {
            activateEncryption(peer, encodedPeerKey);
        }
    }

    private void activateEncryption(User peer, byte[] encodedPeerKey) {
        ClientPeerChat chat = StartClient.manager.getChat(peer);
        chat.setActive(true);
        StartClient.guiManager.setPeerStatus(chat, true);

        chat.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
    }

    private void handleNewChatRequestClient(ConnectPacket packet, Channel channel, Encryption encryption, User peer, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            ClientPeerChat chat = new ClientPeerChat(peer);
            chat.initClient();
            chat.setActive(true);
            StartClient.manager.addChat(chat);

            StartClient.guiManager.setPeerStatus(chat, true);

            chat.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));


            Logger.info("Starting E2E Encryption to: " + peer.getUsername());
            ConnectPacket replyPacket = new ConnectPacket();
            replyPacket.user = peer;
            replyPacket.key = chat.getEncryption().getPublicKey().getEncoded();
            replyPacket.connectType = ConnectPacket.ConnectType.REPLY_CONNECT;
            Logger.trace("Sending packet ConnectPacket to " + peer);

            NetworkingUtility.sendPacket(replyPacket, channel, encryption);
        } else {
            Logger.warn("What is this Client even doing with his life?");
        }
    }
}
