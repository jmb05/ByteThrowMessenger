package net.jmb19905.jmbnetty.client.tcp;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.jmbnetty.client.ClientConnection;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.handler.AbstractChannelHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.util.Logger;

public class TcpClientHandler extends AbstractChannelHandler {

    public TcpClientHandler(ClientConnection connection) {
        super(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Packet packet = (Packet) msg;
            packet.getHandler().handleOnClient(ctx, packet, this);
        }catch (IllegalSideException e){
            Logger.warn(e);
        }
    }
}