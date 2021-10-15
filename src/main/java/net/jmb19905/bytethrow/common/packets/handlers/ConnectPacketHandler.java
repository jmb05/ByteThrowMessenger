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
import net.jmb19905.bytethrow.client.chat.ClientPeerChat;
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
    public void handleOnServer(ChannelHandlerContext channelHandlerContext, Packet packet, TcpServerHandler handler) {
        ConnectPacket connectPacket = (ConnectPacket) packet;
        ServerManager manager = StartServer.manager;
        String clientName = manager.getClientName(handler);
        if (!clientName.isBlank()) {
            String peerName = connectPacket.name;
            if (DatabaseManager.hasUser(peerName)) {
                if (manager.isClientOnline(peerName)) {
                    if (manager.getChat(peerName, clientName) == null) {
                        handleNewChatRequestServer(connectPacket, manager, handler, clientName, peerName);
                    } else if (manager.getChat(peerName, clientName) != null) {
                        handleConnectToExistingChatRequestServer(connectPacket, manager, handler, channelHandlerContext.channel(), clientName, peerName);
                    }
                } else if (connectPacket.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
                    NetworkingUtility.sendFail(channelHandlerContext.channel(), "connect:" + peerName, "not_online", peerName, handler);
                }
            } else {
                NetworkingUtility.sendFail(channelHandlerContext.channel(), "connect:" + peerName, "no_such_user", peerName, handler);
            }
        } else {
            Logger.warn("Client is trying to communicate but isn't logged in!");
        }
    }

    private void handleNewChatRequestServer(ConnectPacket packet, ServerManager manager, TcpServerHandler handler, String clientName, String peerName) {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            PeerChat chat = new PeerChat(clientName, peerName);
            chat.setActive(true);
            manager.addChat(chat);

            ClientDataFilesManager.writeChats(clientName);
            ClientDataFilesManager.writeChats(peerName);

            packet.name = clientName;
            manager.sendPacketToPeer(peerName, packet, handler);
        } else {
            Logger.warn("What is this Client even doing with his life?");
        }
    }

    private void handleConnectToExistingChatRequestServer(ConnectPacket packet, ServerManager manager, TcpServerHandler handler, Channel channel, String clientName, String peerName) {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            NetworkingUtility.sendFail(channel, "connect:" + peerName, "chat_exists", peerName, handler);
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            PeerChat chat = manager.getChat(peerName, clientName);
            chat.setActive(true);

            packet.name = clientName;
            manager.sendPacketToPeer(peerName, packet, handler);
        } else if (packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT) {
            if (manager.isClientOnline(peerName)) {
                packet.name = clientName;
                manager.sendPacketToPeer(peerName, packet, handler);
            }
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT) {
            if (manager.isClientOnline(peerName)) {
                PeerChat chat = manager.getChat(peerName, clientName);
                chat.setActive(true);

                packet.name = clientName;
                manager.sendPacketToPeer(peerName, packet, handler);
            }
        }
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler handler) {
        ConnectPacket connectPacket = (ConnectPacket) packet;
        String peerName = connectPacket.name;
        byte[] encodedPeerKey = connectPacket.key;

        try {
            if (StartClient.manager.getChat(peerName) == null) {
                handleNewChatRequestClient(connectPacket, channelHandlerContext.channel(), handler.getEncryption(), peerName, encodedPeerKey);
            } else if (StartClient.manager.getChat(peerName) != null) {
                handleConnectToExistingChatRequestClient(connectPacket, channelHandlerContext.channel(), handler.getEncryption(), peerName, encodedPeerKey);
            }
        } catch (InvalidKeySpecException e) {
            Logger.error(e);
        }
    }

    private void handleConnectToExistingChatRequestClient(ConnectPacket packet, Channel channel, Encryption encryption, String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Logger.warn("Peer tried create a Chat that already exists");
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            activateEncryption(peerName, encodedPeerKey);
        } else if (packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT) {
            activateEncryption(peerName, encodedPeerKey);

            Encryption chatEncryption = StartClient.manager.getChat(peerName).getEncryption();

            packet.name = peerName;
            packet.key = chatEncryption.getPublicKey().getEncoded();
            packet.connectType = ConnectPacket.ConnectType.REPLY_RECONNECT;

            NetworkingUtility.sendPacket(packet, channel, encryption);
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT) {
            activateEncryption(peerName, encodedPeerKey);
        }
    }

    private void activateEncryption(String peerName, byte[] encodedPeerKey) {
        PeerChat chat = StartClient.manager.getChat(peerName);
        chat.setActive(true);
        StartClient.guiManager.setPeerStatus(peerName, true);

        chat.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
    }

    private void handleNewChatRequestClient(ConnectPacket packet, Channel channel, Encryption encryption, String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            ClientPeerChat chat = new ClientPeerChat(peerName);
            chat.initClient();
            chat.setActive(true);
            StartClient.manager.addChat(chat);

            StartClient.guiManager.setPeerStatus(peerName, true);

            chat.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));


            Logger.info("Starting E2E Encryption to: " + peerName);
            ConnectPacket replyPacket = new ConnectPacket();
            replyPacket.name = peerName;
            replyPacket.key = chat.getEncryption().getPublicKey().getEncoded();
            replyPacket.connectType = ConnectPacket.ConnectType.REPLY_CONNECT;
            Logger.trace("Sending packet ConnectPacket to " + peerName);

            NetworkingUtility.sendPacket(replyPacket, channel, encryption);
        } else {
            Logger.warn("What is this Client even doing with his life?");
        }
    }
}
