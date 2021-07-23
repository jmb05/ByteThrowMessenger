package net.jmb19905.networking.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.crypto.EncryptedConnection;
import net.jmb19905.networking.packets.*;
import net.jmb19905.util.EncryptionUtility;
import net.jmb19905.util.Logger;
import net.jmb19905.util.SerializationUtility;

import java.security.spec.InvalidKeySpecException;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final EncryptedConnection connection = new EncryptedConnection();
    private String name;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now connected", Logger.Level.INFO);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now disconnected", Logger.Level.INFO);
        Server.connections.remove(this);
        try {
            ServerHandler other = (ServerHandler) Server.connections.keySet().toArray()[0];
            SocketChannel channel = (SocketChannel) Server.connections.values().toArray()[0];

            final ByteBuf toOtherBuffer = channel.alloc().buffer();
            toOtherBuffer.writeBytes(other.connection.encrypt(new DisconnectPacket().deconstruct()));
            channel.writeAndFlush(toOtherBuffer);
        }catch (ArrayIndexOutOfBoundsException ignored){
            //Last Client disconnected
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        try {
            Logger.log("Received: " + buffer, Logger.Level.TRACE);

            byte[] encryptedArray = new byte[buffer.readableBytes()];
            buffer.readBytes(encryptedArray);
            byte[] array;
            if(connection.isUsable()){
                array = connection.decrypt(encryptedArray);
            }else {
                array = encryptedArray;
            }
            Packet packet = Packet.constructPacket(array);
            if(packet instanceof KeyExchangePacket){
                if(((KeyExchangePacket) packet).type.equals("Server")) {
                    connection.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(((KeyExchangePacket) packet).key));
                    Logger.log("SharedSecret: " + SerializationUtility.encodeBinary(connection.getSharedSecret()), Logger.Level.TRACE);
                    ((KeyExchangePacket) packet).key = connection.getPublicKey().getEncoded();

                    final ByteBuf replyBuffer = ctx.alloc().buffer();
                    replyBuffer.writeBytes(packet.deconstruct());
                    ctx.writeAndFlush(replyBuffer);
                }else if(((KeyExchangePacket) packet).type.equals("Client")){
                    for(ServerHandler other : Server.connections.keySet()){
                        if(other != this){
                            if(!other.name.isEmpty()){
                                SocketChannel channel = Server.connections.get(other);
                                final ByteBuf toOtherBuffer = channel.alloc().buffer();
                                toOtherBuffer.writeBytes(other.connection.encrypt(packet.deconstruct()));
                                channel.writeAndFlush(toOtherBuffer);
                            }
                        }
                    }
                }
            }else if(packet instanceof LoginPacket){
                name = ((LoginPacket) packet).name;
                Logger.log("Client: " + ctx.channel().remoteAddress() + " now uses name: " + name, Logger.Level.INFO);
                for(ServerHandler other : Server.connections.keySet()){
                    if(other != this){
                        if(!other.name.isEmpty()){
                            SocketChannel channel = Server.connections.get(other);
                            final ByteBuf toOtherBuffer = channel.alloc().buffer();
                            toOtherBuffer.writeBytes(other.connection.encrypt(packet.deconstruct()));
                            channel.writeAndFlush(toOtherBuffer);

                            final ByteBuf replyBuffer = ctx.alloc().buffer();
                            ((LoginPacket) packet).name = other.name;
                            replyBuffer.writeBytes(connection.encrypt(packet.deconstruct()));
                            ctx.writeAndFlush(replyBuffer);
                        }
                    }
                }
            }else if(packet instanceof MessagePacket){
                for(ServerHandler other : Server.connections.keySet()) {
                    if (other != this) {
                        if (!other.name.isEmpty()) {
                            SocketChannel channel = Server.connections.get(other);
                            final ByteBuf toOtherBuffer = channel.alloc().buffer();
                            toOtherBuffer.writeBytes(other.connection.encrypt(packet.deconstruct()));
                            channel.writeAndFlush(toOtherBuffer);
                            Logger.log("Sent message to recipient", Logger.Level.TRACE);
                        }
                    }
                }
            }
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.FATAL);
            System.exit(-1);
        } finally {
            buffer.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.log(cause, Logger.Level.ERROR);
    }
}
