/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.packets.ConnectPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.UserDatabaseManager;
import net.jmb19905.bytethrow.server.networking.ServerManager;
import net.jmb19905.bytethrow.server.util.ClientFileManager;
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
        if(!clientName.isBlank()) {
            String peerName = connectPacket.name;
            if (UserDatabaseManager.hasUser(peerName)) {
                if(manager.isClientOnline(peerName)) {
                    if(manager.getChats(peerName, clientName) == null) {
                        handleNewChatRequestServer(connectPacket, manager, handler, clientName, peerName);
                    }else if(manager.getChats(peerName, clientName) != null) {
                        handleConnectToExistingChatRequestServer(connectPacket, manager, handler, channelHandlerContext.channel(), clientName, peerName);
                    }
                }else if (connectPacket.connectType == ConnectPacket.ConnectType.FIRST_CONNECT){
                    NetworkingUtility.sendFail(channelHandlerContext.channel(), "connect:" + peerName, "not_online", peerName, handler);
                }
            } else {
                NetworkingUtility.sendFail(channelHandlerContext.channel(), "connect:" + peerName, "no_such_user", peerName, handler);
            }
        }else {
            Logger.log("Client is trying to communicate but isn't logged in!", Logger.Level.WARN);
        }
    }

    private void handleNewChatRequestServer(ConnectPacket packet, ServerManager manager, TcpServerHandler handler, String clientName, String peerName) {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Chat chat = new Chat();
            chat.addClient(clientName);
            chat.addClient(peerName);
            chat.setActive(true);
            manager.addChat(chat);

            ClientFileManager.writeChatsToFile(clientName);
            ClientFileManager.writeChatsToFile(peerName);

            packet.name = clientName;
            manager.sendPacketToPeer(peerName, packet, handler);
        }else {
            Logger.log("What is this Client even doing with his life?", Logger.Level.WARN);
        }
    }

    private void handleConnectToExistingChatRequestServer(ConnectPacket packet, ServerManager manager, TcpServerHandler handler, Channel channel, String clientName, String peerName) {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            NetworkingUtility.sendFail(channel, "connect:" + peerName, "chat_exists", peerName, handler);
        }else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            Chat chat = manager.getChats(peerName, clientName);
            chat.setActive(true);

            packet.name = clientName;
            manager.sendPacketToPeer(peerName, packet, handler);
        }else if(packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT){
            if(manager.isClientOnline(peerName)){
                packet.name = clientName;
                manager.sendPacketToPeer(peerName, packet, handler);
            }
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT){
            if(manager.isClientOnline(peerName)){
                Chat chat = manager.getChats(peerName, clientName);
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
            if(StartClient.manager.getChat(peerName) == null) {
                handleNewChatRequestClient(connectPacket, channelHandlerContext.channel(), handler.getEncryption(), peerName, encodedPeerKey);
            }else if(StartClient.manager.getChat(peerName) != null) {
                handleConnectToExistingChatRequestClient(connectPacket, channelHandlerContext.channel(), handler.getEncryption(), peerName, encodedPeerKey);
            }
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.ERROR);
            StartClient.guiManager.appendLine("Error connecting with: " + peerName);
        }
    }

    private void handleConnectToExistingChatRequestClient(ConnectPacket packet, Channel channel, Encryption encryption, String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Logger.warn("Peer tried create a Chat that already exists");
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT){
            activateEncryption(peerName, encodedPeerKey);
        }else if(packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT){
            activateEncryption(peerName, encodedPeerKey);

            Encryption chatEncryption = StartClient.manager.getChat(peerName).encryption;

            packet.name = peerName;
            packet.key = chatEncryption.getPublicKey().getEncoded();
            packet.connectType = ConnectPacket.ConnectType.REPLY_RECONNECT;

            NetworkingUtility.sendPacket(packet, channel, encryption);
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT){
            activateEncryption(peerName, encodedPeerKey);
        }
    }

    private void activateEncryption(String peerName, byte[] encodedPeerKey) {
        Chat chat = StartClient.manager.getChat(peerName);
        chat.setActive(true);
        StartClient.guiManager.setPeerStatus(peerName, true);

        chat.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
        StartClient.guiManager.appendLine("Connection to " + peerName + " encrypted");
    }

    private void handleNewChatRequestClient(ConnectPacket packet, Channel channel, Encryption encryption, String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Chat chat = new Chat();
            chat.addClient(StartClient.manager.name);
            chat.addClient(peerName);
            chat.initClient();
            chat.setActive(true);
            StartClient.manager.chats.add(chat);

            StartClient.guiManager.addPeer(peerName);
            StartClient.guiManager.setPeerStatus(peerName, true);

            chat.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
            StartClient.guiManager.appendLine("Connection to " + peerName + " encrypted");


            Logger.log("Starting E2E Encryption to: " + peerName, Logger.Level.INFO);
            ConnectPacket replyPacket = new ConnectPacket();
            replyPacket.name = peerName;
            replyPacket.key = chat.encryption.getPublicKey().getEncoded();
            replyPacket.connectType = ConnectPacket.ConnectType.REPLY_CONNECT;
            Logger.log("Sending packet ConnectPacket to " + peerName, Logger.Level.TRACE);

            NetworkingUtility.sendPacket(replyPacket, channel, encryption);
        }else {
            Logger.log("What is this Client even doing with his life?", Logger.Level.WARN);
        }
    }
}
