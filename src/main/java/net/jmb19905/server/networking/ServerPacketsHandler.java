package net.jmb19905.server.networking;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.*;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;

public class ServerPacketsHandler {

    private final ServerHandler.ClientConnection connection;
    private final ServerHandler serverHandler;

    public ServerPacketsHandler(ServerHandler serverHandler){
        this.connection = serverHandler.getConnection();
        this.serverHandler = serverHandler;
    }

    /**
     * Handles a constructed incoming Packet
     * @param packet the Packet
     */
    public void handlePacket(ChannelHandlerContext ctx, Packet packet){
        try {
            packet.getServerPacketHandler().handle(serverHandler, connection, ctx.channel());
        } catch (IllegalSideException e) {
            Logger.log(e, Logger.Level.WARN);
        }
    }

    /**
     * Sends a packet to the peer of this client
     * @param packet the packet to be sent
     */
    public static void sendPacketToPeer(String peerName, Packet packet, ServerHandler serverHandler){
        ServerHandler peerHandler = getPeerHandler(peerName, serverHandler);
        SocketChannel channel = Server.connections.get(peerHandler);
        if(peerHandler != null) {
            Logger.log("Sending packet " + packet + " to " + channel.remoteAddress() , Logger.Level.TRACE);
            NetworkingUtility.sendPacket(packet, channel, peerHandler.getConnection().encryption);
        }
    }

    /**
     * @return the ServerHandler of the current peer
     */
    public static ServerHandler getPeerHandler(String name, ServerHandler ownHandler){
        for(ServerHandler peerHandler : Server.connections.keySet()) {
            if (peerHandler != ownHandler) {
                if (peerHandler.getConnection().getName().equals(name) && !peerHandler.getConnection().getName().isBlank()) {
                    return peerHandler;
                }
            }
        }
        return null;
    }

}
