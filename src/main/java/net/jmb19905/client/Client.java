package net.jmb19905.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.jmb19905.common.packets.ConnectPacket;
import net.jmb19905.common.packets.MessagePacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.Chat;
import net.jmb19905.common.util.NetworkingUtility;

import javax.swing.*;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Client
 */
public class Client {

    private EventLoopGroup group;

    private final String host;
    private final int port;
    public String name = "";
    private SocketChannel toServerChannel;
    private ClientHandler handler;

    public List<Chat> chats = new ArrayList<>();

    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }

    /**
     * Starts the Client
     */
    public void start() throws ConnectException {
        group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    toServerChannel = ch;
                    handler = new ClientHandler();
                    ch.pipeline().addLast(new ClientDecoder(handler), handler);
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

    public void stop(){
        group.shutdownGracefully();
        System.exit(0);
    }

    public void connectToPeer(String peerName){
        if(getChat(peerName) != null){
            JOptionPane.showMessageDialog(null, "You have already started a conversation with this peer.", "", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Chat chat = createNewChat(peerName);

        ConnectPacket connectPacket = new ConnectPacket();
        connectPacket.name = peerName;
        connectPacket.key = chat.encryption.getPublicKey().getEncoded();
        connectPacket.connectType = ConnectPacket.ConnectType.FIRST_CONNECT;

        NetworkingUtility.sendPacket(connectPacket, toServerChannel, handler.getEncryption());
        Logger.log("Connecting with peer: " + peerName, Logger.Level.TRACE);
    }

    private Chat createNewChat(String peerName) {
        Chat chat = new Chat();
        chat.initClient();
        chat.addClient(name);
        chat.addClient(peerName);
        chats.add(chat);
        ClientMain.window.addPeer(peerName);
        return chat;
    }

    /**
     * Sends a message to the peer
     * @param message the message as String
     */
    public boolean sendMessage(String recipient, String message){
        MessagePacket packet = new MessagePacket();
        Chat chat = getChat(recipient);
        if(chat != null && chat.isActive()) {
            packet.message = new Chat.Message(name, recipient, EncryptionUtility.encryptString(chat.encryption, message));
            NetworkingUtility.sendPacket(packet, toServerChannel, handler.getEncryption());
            Logger.log("Sent Message: " + message, Logger.Level.TRACE);
            return true;
        }
        return false;
    }

    public Chat getChat(String peerName){
        for(Chat chat : chats){
            if(chat.getClients().contains(peerName)){
                return chat;
            }
        }
        return null;
    }

}
