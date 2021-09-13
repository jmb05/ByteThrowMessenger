package net.jmb19905.common.packets.handlers.server;

import io.netty.channel.Channel;
import net.jmb19905.common.Chat;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.MessagePacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.server.networking.Server;
import net.jmb19905.server.networking.ServerHandler;
import net.jmb19905.server.networking.ServerPacketsHandler;

public class ServerMessagePacketHandler extends ServerPacketHandler<MessagePacket>{

    public ServerMessagePacketHandler(MessagePacket packet) {
        super(packet);
    }

    @Override
    public void handle(ServerHandler serverHandler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        if(connection.getName().equals(packet.message.sender())) {
            String clientName = connection.getName();
            String peerName = packet.message.receiver();
            if (!connection.getName().isBlank()) {
                Chat chat = Server.getChats(clientName, peerName);
                if (chat != null) {
                    if (chat.isActive()) {
                        chat.addMessage(packet.message);
                        ServerPacketsHandler.sendPacketToPeer(peerName, packet, serverHandler);
                        Logger.log("Sent message to recipient: " + peerName, Logger.Level.TRACE);
                    } else {
                        sendFail(channel, "message", "peer_offline", peerName, connection);
                    }
                } else {
                    sendFail(channel, "message", "no_such_chat", peerName, connection);
                }
            } else {
                Logger.log("Client is trying to communicate but isn't logged in!", Logger.Level.WARN);
            }
        }else {
            Logger.log("Received Message with wrong Sender!", Logger.Level.WARN);
        }
    }
}
