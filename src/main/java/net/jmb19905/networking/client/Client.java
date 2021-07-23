package net.jmb19905.networking.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.jmb19905.crypto.EncryptedConnection;
import net.jmb19905.networking.packets.MessagePacket;
import net.jmb19905.util.EncryptionUtility;
import net.jmb19905.util.Logger;

/**
 * The Client
 */
public record Client(String name, String host, int port) {

    public static String peerName = "";
    public static EncryptedConnection peerConnection = new EncryptedConnection();
    private static SocketChannel toServerChannel;
    private static ClientHandler handler;

    /**
     * Starts the Client
     */
    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    toServerChannel = ch;
                    handler = new ClientHandler();
                    ch.pipeline().addLast(handler);
                }
            });
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Logger.log(e, Logger.Level.ERROR);
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * Sends a message to the peer
     * @param message the message as String
     */
    public static void sendMessage(String message){
        MessagePacket packet = new MessagePacket();
        packet.message = EncryptionUtility.encryptString(peerConnection, message);
        ByteBuf buffer = toServerChannel.alloc().buffer();
        buffer.writeBytes(handler.getConnection().encrypt(packet.deconstruct()));
        toServerChannel.writeAndFlush(buffer);
        Logger.log("Sent Message: " + message, Logger.Level.TRACE);
    }

}
