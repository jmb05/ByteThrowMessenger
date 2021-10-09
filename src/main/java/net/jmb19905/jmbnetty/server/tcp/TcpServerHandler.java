package net.jmb19905.jmbnetty.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.jmbnetty.common.handler.AbstractChannelHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.ServerConnection;

public class TcpServerHandler extends AbstractChannelHandler {

    public TcpServerHandler(ServerConnection connection) {
        super(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Packet packet = (Packet) msg;
        packet.getHandler().handleOnServer(ctx, packet, this);
    }

}