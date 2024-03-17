package net.jmb19905.bytethrow.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.UnixChannel;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class UdsClient {

    public void run() throws InterruptedException {
        try (EpollEventLoopGroup events = new EpollEventLoopGroup()) {
            Bootstrap bootstrap = new Bootstrap()
                    .group(events)
                    .channel(EpollDomainSocketChannel.class)
                    .handler(new ChannelInitializer<UnixChannel>() {
                        @Override
                        protected void initChannel(@NotNull UnixChannel ch) throws Exception {
                            ch.pipeline().addLast(new UdsClientHandler());
                        }
                    });
            var channel = bootstrap.connect(new DomainSocketAddress("/tmp/netty.sock")).sync().channel();
            ByteBuf buf = channel.alloc().buffer(4 + 5);
            buf.writeInt(5);
            buf.writeCharSequence("Hello", StandardCharsets.UTF_8);
            channel.writeAndFlush(buf);
            channel.closeFuture().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new UdsClient().run();
    }

}
