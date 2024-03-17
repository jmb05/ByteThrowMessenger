package net.jmb19905.bytethrow.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public class UdsEchoServer {

    public void run() throws Exception {
        try (EventLoopGroup bossGroup = new EpollEventLoopGroup(); EventLoopGroup workerGroup = new EpollEventLoopGroup()) {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 4096)
                    .channel(EpollServerDomainSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(@NotNull Channel ch) {
                            ch.pipeline().addLast(new UdsEchoServerHandler());
                        }
                    });
            SocketAddress s = new DomainSocketAddress("/tmp/netty.sock");
            ChannelFuture f = b.bind(s);
            f.channel().closeFuture().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        new UdsEchoServer().run();
    }

}
