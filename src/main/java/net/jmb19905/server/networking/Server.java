package net.jmb19905.server.networking;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.jmb19905.common.Chat;
import net.jmb19905.server.util.ClientFileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Server
 */
public record Server(int port) {

    /**
     * The Handlers for all connected clients and their addresses
     */
    public static final Map<ServerHandler, SocketChannel> connections = new HashMap<>();

    public static final List<Chat> chats = new ArrayList<>();

    /**
     * Starts the Server
     */
    public void run() throws Exception{
        ClientFileManager.loadAllClientFiles();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ServerHandler handler = new ServerHandler();
                            connections.put(handler, ch);
                            ch.pipeline().addLast(new ServerDecoder(handler), handler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static Chat getChats(String user1, String user2){
        for(Chat chat : chats){
            List<String> users = chat.getClients();
            if(users.contains(user1) && users.contains(user2)){
                return chat;
            }
        }
        return null;
    }

    public static List<Chat> getChats(String user){
        List<Chat> chatsContainingUser = new ArrayList<>();
        for(Chat chat : chats){
            List<String> users = chat.getClients();
            if(users.contains(user)){
                chatsContainingUser.add(chat);
            }
        }
        return chatsContainingUser;
    }

    public static boolean isClientOnline(String name){
        for (ServerHandler handler : connections.keySet()) {
            if (handler.getConnection().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static void changeName(String oldName, String newName){
        for (Chat chat : chats) {
            List<String> chatParticipants = chat.getClients();
            chatParticipants.remove(oldName);
            chatParticipants.add(newName);
            chat.setClients(chatParticipants);
        }
    }

    public static String[] getPeerNames(String clientName) {
        List<String> names = new ArrayList<>();
        for (Chat chat : chats) {
            List<String> chatParticipants = chat.getClients();
            if (chatParticipants.contains(clientName)) {
                for (String otherName : chatParticipants) {
                    if (!otherName.equals(clientName)) {
                        names.add(otherName);
                    }
                }
            }
        }
        return names.toArray(new String[0]);
    }

}