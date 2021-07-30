package net.jmb19905.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.CreateChatPacket;
import net.jmb19905.common.packets.MessagePacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.server.Chat;

/**
 * The Client
 */
public class Client {

    private final String host;
    private final int port;

    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }

    public String name = "";
    public String peerName = "";
    public EncryptedConnection peerConnection = new EncryptedConnection();
    private SocketChannel toServerChannel;
    private ClientHandler handler;

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

    public void connectToPeer(String peerName){
        CreateChatPacket createChatPacket = new CreateChatPacket();
        createChatPacket.name = peerName;
        ByteBuf buffer = toServerChannel.alloc().buffer();
        buffer.writeBytes(handler.getConnection().encrypt(createChatPacket.deconstruct()));
        toServerChannel.writeAndFlush(buffer);
        Logger.log("Connecting with peer: " + peerName, Logger.Level.TRACE);
    }

    /**
     * Sends a message to the peer
     * @param message the message as String
     */
    public void sendMessage(String recipient, String message){
        MessagePacket packet = new MessagePacket();
        packet.message = new Chat.Message(recipient, EncryptionUtility.encryptString(peerConnection, message));
        ByteBuf buffer = toServerChannel.alloc().buffer();
        buffer.writeBytes(handler.getConnection().encrypt(packet.deconstruct()));
        toServerChannel.writeAndFlush(buffer);
        Logger.log("Sent Message: " + message, Logger.Level.TRACE);
    }
}
