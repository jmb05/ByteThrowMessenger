package net.jmb19905.jmbnetty.server.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.jmb19905.jmbnetty.common.handler.Decoder;
import net.jmb19905.jmbnetty.server.ServerConnection;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import java.util.HashMap;
import java.util.Map;

public class TcpServerConnection extends ServerConnection {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final Map<TcpServerHandler, SocketChannel> clientConnections = new HashMap<>();

    public TcpServerConnection(int port) {
        super(port);
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void run() {
        TcpServerConnection instance = this;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            TcpServerHandler serverHandler = new TcpServerHandler(instance);
                            ch.pipeline().addLast(new Decoder(serverHandler.getEncryption()), serverHandler);
                            clientConnections.put(serverHandler, ch);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.bind(port).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            Logger.fatal(e);
            ShutdownManager.shutdown(-1);
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        super.stop();
    }

    public Map<TcpServerHandler, SocketChannel> getClientConnections() {
        return clientConnections;
    }
}
