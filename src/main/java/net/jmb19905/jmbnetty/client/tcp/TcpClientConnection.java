package net.jmb19905.jmbnetty.client.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.jmb19905.jmbnetty.client.ClientConnection;
import net.jmb19905.jmbnetty.common.handler.Decoder;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

public class TcpClientConnection extends ClientConnection {

    private final EventLoopGroup group;
    private TcpClientHandler clientHandler;
    private SocketChannel channel;

    public TcpClientConnection(int port, String remoteAddress) {
        super(port, remoteAddress);
        this.group = new NioEventLoopGroup();
    }

    @Override
    public void run() {
        clientHandler = new TcpClientHandler(this);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new Decoder(clientHandler.getEncryption()), clientHandler);
                            channel = ch;
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);
            b.connect(getRemoteAddress(), getPort()).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            Logger.fatal(e, "Client closed in non-standard way -> crashing");
            ShutdownManager.shutdown(-1);
        }
    }

    public TcpClientHandler getClientHandler() {
        return clientHandler;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void stop() {
        group.shutdownGracefully();
        super.stop();
    }
}
