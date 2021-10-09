package net.jmb19905.jmbnetty.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jmb19905.jmbnetty.common.connection.AbstractConnection;
import net.jmb19905.jmbnetty.common.connection.event.ConnectedEvent;
import net.jmb19905.jmbnetty.common.connection.event.DisconnectedEvent;
import net.jmb19905.jmbnetty.common.connection.event.ErrorEvent;
import net.jmb19905.util.Logger;

public abstract class AbstractChannelHandler extends ChannelInboundHandlerAdapter {

    private final AbstractConnection connection;

    public AbstractChannelHandler(AbstractConnection connection){
        this.connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        connection.performEvent("connected", () -> new ConnectedEvent(this));
    }

    @Override
    public abstract void channelRead(ChannelHandlerContext ctx, Object msg);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        connection.performEvent("disconnected", () -> new DisconnectedEvent(this));
        connection.markClosed();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        connection.performEvent("error", () -> new ErrorEvent(this, cause));
        Logger.error(cause);
    }

    public AbstractConnection getConnection() {
        return connection;
    }
}