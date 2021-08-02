package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.ConnectPacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.ClientFileManager;
import net.jmb19905.server.Server;
import net.jmb19905.server.ServerHandler;
import net.jmb19905.server.ServerPacketHandler;
import net.jmb19905.server.database.SQLiteManager;

import java.security.spec.InvalidKeySpecException;

public class ConnectPacketHandler extends PacketHandler<ConnectPacket> {

    @Override
    public void handleOnServer(ConnectPacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) {
        String clientName = connection.getName();
        if(!clientName.isBlank()) {
            String peerName = packet.name;
            if (SQLiteManager.hasUser(peerName)) {
                if(Server.isClientOnline(peerName)) {
                    if(Server.getChats(peerName, clientName) == null) {
                        handleNewChatRequestServer(packet, handler, clientName, peerName);
                    }else if(Server.getChats(peerName, clientName) != null) {
                        handleConnectToExistingChatRequestServer(packet, handler, connection, channel, clientName, peerName);
                    }
                }else if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT){
                    sendFail(channel, "connect:" + peerName, "User: " + peerName + " not online!", connection);
                }
            } else {
                sendFail(channel, "connect:" + peerName, "No such User: " + peerName, connection);
            }
        }else {
            Logger.log("Client is trying to communicate but isn't logged in!", Logger.Level.WARN);
        }
    }

    private void handleNewChatRequestServer(ConnectPacket packet, ServerHandler handler, String clientName, String peerName) {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Chat chat = new Chat();
            chat.addClient(clientName);
            chat.addClient(peerName);
            chat.setActive(true);
            Server.chats.add(chat);
            Logger.log("Added Chat:" + chat, Logger.Level.DEBUG);

            ClientFileManager.writeChatsToFile(clientName);
            ClientFileManager.writeChatsToFile(peerName);

            packet.name = clientName;
            ServerPacketHandler.sendPacketToPeer(peerName, packet, handler);
        }else {
            Logger.log("What is this Client even doing with his life?", Logger.Level.WARN);
        }
    }

    private void handleConnectToExistingChatRequestServer(ConnectPacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel, String clientName, String peerName) {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            sendFail(channel, "connect:" + peerName, "Chat with: " + peerName + " already exists!", connection);
        }else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            Chat chat = Server.getChats(peerName, clientName);
            chat.setActive(true);

            packet.name = clientName;
            ServerPacketHandler.sendPacketToPeer(peerName, packet, handler);
        }else if(packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT){
            if(Server.isClientOnline(peerName)){
                packet.name = clientName;
                ServerPacketHandler.sendPacketToPeer(peerName, packet, handler);
            }
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT){
            if(Server.isClientOnline(peerName)){
                Chat chat = Server.getChats(peerName, clientName);
                chat.setActive(true);

                packet.name = clientName;
                ServerPacketHandler.sendPacketToPeer(peerName, packet, handler);
            }
        }
    }

    @Override
    public void handleOnClient(ConnectPacket packet, EncryptedConnection encryption, Channel channel) {
        String peerName = packet.name;
        byte[] encodedPeerKey = packet.key;

        try {
            if(ClientMain.client.getChat(peerName) == null) {
                handleNewChatRequestClient(packet, channel, encryption, peerName, encodedPeerKey);
            }else if(ClientMain.client.getChat(peerName) != null) {
                handleConnectToExistingChatRequestClient(packet, channel, encryption, peerName, encodedPeerKey);
            }
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.ERROR);
            ClientMain.window.appendLine("Error connecting with: " + peerName);
        }
    }

    private void handleConnectToExistingChatRequestClient(ConnectPacket packet, Channel channel, EncryptedConnection encryption, String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Logger.log("Peer tried create a Chat that already exists", Logger.Level.WARN);
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT){
            activateEncryption(peerName, encodedPeerKey);
        }else if(packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT){
            activateEncryption(peerName, encodedPeerKey);

            EncryptedConnection chatEncryption = ClientMain.client.getChat(peerName).encryption;

            packet.name = peerName;
            packet.key = chatEncryption.getPublicKey().getEncoded();
            packet.connectType = ConnectPacket.ConnectType.REPLY_RECONNECT;

            NetworkingUtility.sendPacket(packet, channel, encryption);
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT){
            activateEncryption(peerName, encodedPeerKey);
        }
    }

    private void activateEncryption(String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        Chat chat = ClientMain.client.getChat(peerName);
        chat.setActive(true);
        ClientMain.window.setPeerStatus(peerName, true);

        chat.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
        ClientMain.window.appendLine("Connection to " + peerName + " encrypted");
    }

    private void handleNewChatRequestClient(ConnectPacket packet, Channel channel, EncryptedConnection encryption, String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Chat chat = new Chat();
            chat.addClient(ClientMain.client.name);
            chat.addClient(peerName);
            chat.initClient();
            chat.setActive(true);
            ClientMain.client.chats.add(chat);

            ClientMain.window.addPeer(peerName);
            ClientMain.window.setPeerStatus(peerName, true);

            chat.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
            ClientMain.window.appendLine("Connection to " + peerName + " encrypted");


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
