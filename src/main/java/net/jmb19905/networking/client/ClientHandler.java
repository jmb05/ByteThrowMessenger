package net.jmb19905.networking.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jmb19905.crypto.EncryptedConnection;
import net.jmb19905.networking.packets.*;
import net.jmb19905.util.EncryptionUtility;
import net.jmb19905.util.Logger;
import net.jmb19905.util.SerializationUtility;

import java.nio.charset.StandardCharsets;
import java.security.spec.InvalidKeySpecException;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final EncryptedConnection connection = new EncryptedConnection();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Logger.log("Connected to Server", Logger.Level.INFO);
        ClientMain.window.appendLine("Connected to Server");

        KeyExchangePacket packet = new KeyExchangePacket();
        packet.key = connection.getPublicKey().getEncoded();

        final ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(packet.deconstruct());

        Logger.log("Sending buffer:" + buffer, Logger.Level.TRACE);
        ctx.writeAndFlush(buffer);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.log("Disconnected from Server", Logger.Level.INFO);
        ClientMain.window.appendLine("Disconnected from Server");
        ClientMain.window.dispose();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        try {
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
                    Logger.log("SharedSecret to Server: " + SerializationUtility.encodeBinary(connection.getSharedSecret()), Logger.Level.TRACE);
                    ClientMain.window.appendLine("Connection to Server encrypted");

                    LoginPacket loginPacket = new LoginPacket();
                    loginPacket.name = ClientMain.client.name();
                    ByteBuf loginBuffer = ctx.alloc().buffer();
                    loginBuffer.writeBytes(connection.encrypt(loginPacket.deconstruct()));
                    ctx.writeAndFlush(loginBuffer);
                }else if(((KeyExchangePacket) packet).type.equals("Client")){
                    Client.otherConnection.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(((KeyExchangePacket) packet).key));
                    Logger.log("SharedSecret to Other Client: " + SerializationUtility.encodeBinary(connection.getSharedSecret()), Logger.Level.TRACE);
                    ClientMain.window.appendLine("Connection to " + Client.otherName + " encrypted");
                }
            }else if(packet instanceof LoginPacket){
                Client.otherName = ((LoginPacket) packet).name;
                ClientMain.window.appendLine(Client.otherName + " joined");

                KeyExchangePacket keyExchangePacket = new KeyExchangePacket();
                keyExchangePacket.type = "Client";
                keyExchangePacket.key = Client.otherConnection.getPublicKey().getEncoded();

                ByteBuf keyPackBuffer = ctx.alloc().buffer();
                keyPackBuffer.writeBytes(connection.encrypt(keyExchangePacket.deconstruct()));
                ctx.writeAndFlush(keyPackBuffer);
            }else if(packet instanceof DisconnectPacket){
                ClientMain.window.appendLine(Client.otherName + " left");
                Client.otherName = "";
                Client.otherConnection = new EncryptedConnection();
            }else if(packet instanceof MessagePacket){
                ClientMain.window.appendLine("<" + Client.otherName + "> " + new String(Client.otherConnection.decrypt(((MessagePacket) packet).message.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
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
        ctx.close();
    }

    public EncryptedConnection getConnection() {
        return connection;
    }
}
