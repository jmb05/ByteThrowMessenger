package net.jmb19905.common.packets.handlers.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.FailPacket;
import net.jmb19905.common.packets.Packet;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.networking.ServerHandler;

public abstract class ServerPacketHandler<P extends Packet> {

    protected P packet;

    public ServerPacketHandler(P packet){
        this.packet = packet;
    }

    public abstract void handle(ServerHandler serverHandler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException;

    /**
     * Send a Packet to the client to tell him that something failed
     * @param cause the cause of the fail e.g. login or register
     * @param message the message displayed to the client
     */
    protected ChannelFuture sendFail(Channel channel, String cause, String message, String extra, ServerHandler.ClientConnection connection) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        failPacket.extra = extra;
        return NetworkingUtility.sendPacket(failPacket, channel, connection.encryption);
    }

}
