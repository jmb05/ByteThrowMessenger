package net.jmb19905.networking.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.crypto.EncryptedConnection;
import net.jmb19905.exception.InvalidUsernameException;
import net.jmb19905.networking.packets.*;
import net.jmb19905.util.EncryptionUtility;
import net.jmb19905.util.Logger;
import net.jmb19905.util.SerializationUtility;

import java.security.spec.InvalidKeySpecException;

/**
 * The server-side Handler for the server-client connection
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * The Encryption to thw Server
     */
    private final EncryptedConnection connection = new EncryptedConnection();

    /**
     * Name of the client
     */
    private String name;

    /**
     * Executes when the Connection to the Server starts
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now connected", Logger.Level.INFO);
    }

    /**
     * Executes when the connection to the server drops
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now disconnected", Logger.Level.INFO);
        Server.connections.remove(this);
        try {
            ServerHandler peerHandler = (ServerHandler) Server.connections.keySet().toArray()[0];
            SocketChannel channel = (SocketChannel) Server.connections.values().toArray()[0];

            final ByteBuf toOtherBuffer = channel.alloc().buffer();
            toOtherBuffer.writeBytes(peerHandler.connection.encrypt(new DisconnectPacket().deconstruct()));
            channel.writeAndFlush(toOtherBuffer);
        }catch (ArrayIndexOutOfBoundsException ignored){
            //Last Client disconnected
        }
    }

    /**
     * Executes when a packet from the client is received
     * @param msg the packet as Object
     */
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
                handleKeyPacket(ctx, (KeyExchangePacket) packet);
            }else if(packet instanceof LoginPacket){
                name = ((LoginPacket) packet).name;
                Logger.log("Client: " + ctx.channel().remoteAddress() + " now uses name: " + name, Logger.Level.INFO);
                forwardNameToPeer(ctx, (LoginPacket) packet);
            }else if(packet instanceof MessagePacket){
                forwardMessageToPeer((MessagePacket) packet);
            }
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.FATAL);
            System.exit(-1);
        } catch (InvalidUsernameException e) {
            Logger.log(e, "User has invalid username -> closing connection", Logger.Level.WARN);
            ctx.close();
        } finally {
            buffer.release();
        }
    }

    /**
     * Handles a KeyExchangePacket
     * @param packet the KeyExchangePacket
     * @throws InvalidKeySpecException if the Key is invalid
     */
    private void handleKeyPacket(ChannelHandlerContext ctx, KeyExchangePacket packet) throws InvalidKeySpecException {
        if(packet.type.equals("Server")) {
            activateEncryptionAndReply(packet, ctx);
        }else if(packet.type.equals("Client")){
            forwardKeyToPeer(packet);
        }
    }

    /**
     * Sends message to the current client's peer
     * @param packet the MessagePacket
     */
    private void forwardMessageToPeer(MessagePacket packet) {
        for(ServerHandler peerHandler : Server.connections.keySet()) {
            if (peerHandler != this) {
                if (!peerHandler.name.isEmpty()) {
                    SocketChannel channel = Server.connections.get(peerHandler);
                    final ByteBuf toOtherBuffer = channel.alloc().buffer();
                    toOtherBuffer.writeBytes(peerHandler.connection.encrypt(packet.deconstruct()));
                    channel.writeAndFlush(toOtherBuffer);
                    Logger.log("Sent message to recipient", Logger.Level.TRACE);
                }
            }
        }
    }

    /**
     * Sends LoginPacket to peer to inform him about the client's name
     * @param packet the LoginPacket
     */
    private void forwardNameToPeer(ChannelHandlerContext ctx, LoginPacket packet) {
        for(ServerHandler peerHandler : Server.connections.keySet()){
            if(peerHandler != this){
                if(!peerHandler.name.isEmpty()){
                    SocketChannel channel = Server.connections.get(peerHandler);
                    final ByteBuf toOtherBuffer = channel.alloc().buffer();
                    toOtherBuffer.writeBytes(peerHandler.connection.encrypt(packet.deconstruct()));
                    channel.writeAndFlush(toOtherBuffer);

                    final ByteBuf replyBuffer = ctx.alloc().buffer();
                    packet.name = peerHandler.name;
                    replyBuffer.writeBytes(connection.encrypt(packet.deconstruct()));
                    ctx.writeAndFlush(replyBuffer);
                }
            }
        }
    }

    /**
     * Sends KeyExchangePacket to peer
     * @param packet the KeyExchangePacket
     */
    private void forwardKeyToPeer(KeyExchangePacket packet) {
        for(ServerHandler peerHandler : Server.connections.keySet()){
            if(peerHandler != this){
                if(!peerHandler.name.isEmpty()){
                    SocketChannel channel = Server.connections.get(peerHandler);
                    final ByteBuf toOtherBuffer = channel.alloc().buffer();
                    toOtherBuffer.writeBytes(peerHandler.connection.encrypt(packet.deconstruct()));
                    channel.writeAndFlush(toOtherBuffer);
                }
            }
        }
    }

    /**
     * Creates the SharedSecret-Key to the client from the client's PublicKey and the PrivateKey and replies with the own PublicKey
     * @param packet the packet containing the encoded PublicKey
     * @throws InvalidKeySpecException if the PublicKey of the client is invalid
     */
    private void activateEncryptionAndReply(KeyExchangePacket packet, ChannelHandlerContext ctx) throws InvalidKeySpecException {
        connection.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
        Logger.log("SharedSecret: " + SerializationUtility.encodeBinary(connection.getSharedSecret()), Logger.Level.TRACE);
        packet.key = connection.getPublicKey().getEncoded();

        final ByteBuf replyBuffer = ctx.alloc().buffer();
        replyBuffer.writeBytes(packet.deconstruct());
        ctx.writeAndFlush(replyBuffer);
    }

    /**
     * Executed if an exception is caught
     * @param cause the Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.log(cause, Logger.Level.ERROR);
    }
}
