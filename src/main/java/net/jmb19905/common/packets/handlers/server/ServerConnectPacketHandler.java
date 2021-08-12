package net.jmb19905.common.packets.handlers.server;

import io.netty.channel.Channel;
import net.jmb19905.common.Chat;
import net.jmb19905.common.packets.ConnectPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.server.database.UserDatabaseManager;
import net.jmb19905.server.networking.Server;
import net.jmb19905.server.networking.ServerHandler;
import net.jmb19905.server.networking.ServerPacketsHandler;
import net.jmb19905.server.util.ClientFileManager;

public class ServerConnectPacketHandler extends ServerPacketHandler<ConnectPacket> {

    public ServerConnectPacketHandler(ConnectPacket packet) {
        super(packet);
    }

    @Override
    public void handle(ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) {
        String clientName = connection.getName();
        if(!clientName.isBlank()) {
            String peerName = packet.name;
            if (UserDatabaseManager.hasUser(peerName)) {
                if(Server.isClientOnline(peerName)) {
                    if(Server.getChats(peerName, clientName) == null) {
                        handleNewChatRequestServer(packet, handler, clientName, peerName);
                    }else if(Server.getChats(peerName, clientName) != null) {
                        handleConnectToExistingChatRequestServer(packet, handler, connection, channel, clientName, peerName);
                    }
                }else if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT){
                    sendFail(channel, "connect:" + peerName, "not_online", peerName, connection);
                }
            } else {
                sendFail(channel, "connect:" + peerName, "no_such_user", peerName, connection);
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
            ServerPacketsHandler.sendPacketToPeer(peerName, packet, handler);
        }else {
            Logger.log("What is this Client even doing with his life?", Logger.Level.WARN);
        }
    }

    private void handleConnectToExistingChatRequestServer(ConnectPacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel, String clientName, String peerName) {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            sendFail(channel, "connect:" + peerName, "chat_exists", peerName, connection);
        }else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            Chat chat = Server.getChats(peerName, clientName);
            chat.setActive(true);

            packet.name = clientName;
            ServerPacketsHandler.sendPacketToPeer(peerName, packet, handler);
        }else if(packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT){
            if(Server.isClientOnline(peerName)){
                packet.name = clientName;
                ServerPacketsHandler.sendPacketToPeer(peerName, packet, handler);
            }
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT){
            if(Server.isClientOnline(peerName)){
                Chat chat = Server.getChats(peerName, clientName);
                chat.setActive(true);

                packet.name = clientName;
                ServerPacketsHandler.sendPacketToPeer(peerName, packet, handler);
            }
        }
    }
}
