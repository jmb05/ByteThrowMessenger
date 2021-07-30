package net.jmb19905.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.InvalidLoginException;
import net.jmb19905.common.packets.*;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.SerializationUtility;
import net.jmb19905.server.database.SQLiteManager;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.spec.InvalidKeySpecException;

/**
 * The server-side Handler for the server-client connection
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * The connection details of the client
     */
    private ClientConnection connection;

    /**
     * Executes when the Connection to the Server starts
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now connected", Logger.Level.INFO);
        connection = new ClientConnection(ctx.channel(), new EncryptedConnection());
    }

    /**
     * Executes when the connection to the server drops
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now disconnected", Logger.Level.INFO);
        Server.connections.remove(this);
        notifyPeerOfDisconnect();
    }

    /**
     * Executes when a packet from the client is received
     * @param msg the packet as Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        try {
            Packet packet = getPacket(buffer);
            handlePacket(ctx, packet);
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.ERROR);
        } catch (InvalidLoginException e) {
            Logger.log(e, "User has invalid username -> closing connection", Logger.Level.WARN);
            ctx.close();
        } finally {
            buffer.release();
        }
    }

    private Packet getPacket(ByteBuf buffer) throws InvalidLoginException {
        byte[] encryptedData = new byte[buffer.readableBytes()];
        buffer.readBytes(encryptedData);
        byte[] data = decryptData(encryptedData);

        Packet packet = Packet.constructPacket(data);
        Logger.log("Decoded Packet: " + packet, Logger.Level.TRACE);
        return packet;
    }

    private byte[] decryptData(byte[] encryptedArray) {
        byte[] array;
        if(connection.encryption.isUsable()){
            array = connection.encryption.decrypt(encryptedArray);
        }else {
            array = encryptedArray;
        }
        return array;
    }

    private void handlePacket(ChannelHandlerContext ctx, Packet packet) throws InvalidKeySpecException {
        if(packet instanceof KeyExchangePacket){
            handleKeyPacket(ctx, (KeyExchangePacket) packet);
        }else if(packet instanceof LoginPacket){
            handleLoginPacket(ctx, packet);
        }else if(packet instanceof MessagePacket){
            handleMessagePacket((MessagePacket) packet);
        }else if(packet instanceof CreateChatPacket){
            handleCreateChatPacket(ctx, (CreateChatPacket) packet);
        }
    }

    private void handleCreateChatPacket(ChannelHandlerContext ctx, CreateChatPacket packet) {
        if(!connection.name.isBlank()) {
            if (SQLiteManager.hasUser(packet.name)) {
                Chat chat = new Chat();
                chat.addClient(connection.getName());
                chat.addClient(packet.name);
                Server.chats.add(chat);
            } else {
                sendFail(ctx, "chat", "No such User!");
            }
        }else {
            Logger.log("Client is trying to communicate but isn't logged in!", Logger.Level.WARN);
        }
    }

    private void handleMessagePacket(MessagePacket packet) {
        if(!connection.name.isBlank()) {
            forwardMessageToPeer(packet.message.sender(), packet);//TODO: implement with Chat
        }else {
            Logger.log("Client is trying to communicate but isn't logged in!", Logger.Level.WARN);
        }
    }

    private void handleLoginPacket(ChannelHandlerContext ctx, Packet packet) {
        System.out.println(new String(packet.deconstruct(), StandardCharsets.UTF_8));
        if(((LoginPacket) packet).register){
            handleRegister(ctx, (LoginPacket) packet);
        }else {
            handleLogin(ctx, (LoginPacket) packet);
        }
    }

    private void handleLogin(ChannelHandlerContext ctx, LoginPacket packet) {
        if(SQLiteManager.hasUser(packet.name)){
            SQLiteManager.UserData userData = SQLiteManager.getUserByName(packet.name);
            if(BCrypt.checkpw(packet.password, userData.password())){
                handleSuccessfulLogin(ctx, packet);
            }else {
                sendFail(ctx, "login", "Failed to Login! - wrong password");
            }
        }else {
            sendFail(ctx, "login", "Failed to Login! - User not found");
        }
    }

    private void handleRegister(ChannelHandlerContext ctx, LoginPacket packet) {
        Logger.log("User is trying to register", Logger.Level.TRACE);
        if(SQLiteManager.createUser(packet.name, packet.password)){
            handleSuccessfulLogin(ctx, packet);
        }else {
            sendFail(ctx, "register", "Failed to Register!");
        }
    }

    /**
     * Send a Packet to the client to tell him that something failed
     * @param cause the cause of the fail e.g. login or register
     * @param message the message displayed to the client
     */
    private void sendFail(ChannelHandlerContext ctx, String cause, String message) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        ByteBuf failBuffer = ctx.alloc().buffer();
        failBuffer.writeBytes(connection.encryption.encrypt(failPacket.deconstruct()));
        ctx.writeAndFlush(failBuffer);
    }

    /**
     * Things to do when a client logs in: -> set the client name -> create client file if it doesn't exist yet ->
     * tell the Client that the login succeeded -> tell the client which conversations he has started
     * @param packet the login packet containing the login packet of the client
     */
    private void handleSuccessfulLogin(ChannelHandlerContext ctx, LoginPacket packet) {
        connection.setName(packet.name);
        Logger.log("Client: " + ctx.channel().remoteAddress() + " now uses name: " + connection.getName(), Logger.Level.INFO);

        createClientFile();

        DataPacket dataPacket = new DataPacket();
        dataPacket.type = "response";

        replyToLogin(ctx, packet); // confirms the login to the current client
        //forwardNameToPeer(packet);//send the peer the name of the current client TODO: look at this
        //sendPeerInformation(ctx);// send the current client the name of the peer
    }

    /**
     * creates the client file if it doesn't exist yet
     */
    private void createClientFile() {
        try {
            File clientFile = new File("clientData/" + connection.getName() + ".dat");
            if(!clientFile.exists()){
                clientFile.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a KeyExchangePacket
     * @param packet the KeyExchangePacket
     * @throws InvalidKeySpecException if the Key is invalid
     */
    private void handleKeyPacket(ChannelHandlerContext ctx, KeyExchangePacket packet) throws InvalidKeySpecException {
        if(packet.recipient.equals("Server")) {
            activateEncryptionAndReply(packet, ctx);
        }else{
            connectClientWithPeer(connection.getName(), packet);//TODO: check if this is the correct name
        }
    }

    /**
     * Sends message to the current client's peer
     * @param packet the MessagePacket
     */
    private void forwardMessageToPeer(String name, MessagePacket packet) {
        sendPacketToPeer(name, packet);
        Logger.log("Sent message to recipient", Logger.Level.TRACE);
    }

    /*
    /**
     * Sends LoginPacket to peer to inform him about the client's name
     * @param packet the LoginPacket
     *
    private void forwardNameToPeer(LoginPacket packet) {
        sendPacketToPeer(packet);
    }*/

    /*
    /**
     * Tells the client the name of the current peer
     *
    private void sendPeerInformation(ChannelHandlerContext ctx){
        ServerHandler peerHandler = getPeerHandler();
        final ByteBuf replyBuffer = ctx.alloc().buffer();
        LoginPacket packet = new LoginPacket();
        packet.name = peerHandler.name;
        packet.password = "null";
        replyBuffer.writeBytes(connection.encrypt(packet.deconstruct()));
        Logger.log("Sending packet " + new String(packet.deconstruct(), StandardCharsets.UTF_8) + " to " + ctx.channel().remoteAddress() , Logger.Level.TRACE);
        ctx.writeAndFlush(replyBuffer);
    }*/

    /**
     * Sends LoginPacket to client to confirm login
     * @param packet the LoginPacket
     */
    private void replyToLogin(ChannelHandlerContext ctx, LoginPacket packet) {
        ByteBuf replyBuffer = ctx.alloc().buffer();
        replyBuffer.writeBytes(connection.encryption.encrypt(packet.deconstruct()));
        Logger.log("Sending packet " + new String(packet.deconstruct(), StandardCharsets.UTF_8) + " to " + ctx.channel().remoteAddress() , Logger.Level.TRACE);
        ctx.writeAndFlush(replyBuffer);
    }

    /**
     * Sends KeyExchangePacket to peer
     * @param packet the KeyExchangePacket
     */
    private void connectClientWithPeer(String name, KeyExchangePacket packet) {
        sendPacketToPeer(name, packet);
    }

    /**
     * Creates the SharedSecret-Key to the client from the client's PublicKey and the PrivateKey and replies with the own PublicKey
     * @param packet the packet containing the encoded PublicKey
     * @throws InvalidKeySpecException if the PublicKey of the client is invalid
     */
    private void activateEncryptionAndReply(KeyExchangePacket packet, ChannelHandlerContext ctx) throws InvalidKeySpecException {
        connection.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
        Logger.log("SharedSecret: " + SerializationUtility.encodeBinary(connection.encryption.getSharedSecret()), Logger.Level.TRACE);
        packet.key = connection.encryption.getPublicKey().getEncoded();

        final ByteBuf replyBuffer = ctx.alloc().buffer();
        replyBuffer.writeBytes(packet.deconstruct());
        Logger.log("Sending packet " + new String(packet.deconstruct(), StandardCharsets.UTF_8) + " to " + ctx.channel().remoteAddress() , Logger.Level.TRACE);
        ctx.writeAndFlush(replyBuffer);
    }

    /**
     * @return the ServerHandler of the current peer
     */
    private ServerHandler getPeerHandler(String name){
        for(ServerHandler peerHandler : Server.connections.keySet()) {
            if (peerHandler != this) {
                if (peerHandler.connection.name.equals(name) && !peerHandler.connection.name.isBlank()) {
                    return peerHandler;
                }
            }
        }
        return null;
    }

    /**
     * Sends a packet to the peer of this client
     * @param packet the packet to be sent
     */
    private void sendPacketToPeer(String name, Packet packet){
        ServerHandler peerHandler = getPeerHandler(name);
        SocketChannel channel = Server.connections.get(peerHandler);
        if(peerHandler != null) {
            final ByteBuf buffer = channel.alloc().buffer();
            buffer.writeBytes(peerHandler.connection.encryption.encrypt(packet.deconstruct()));
            Logger.log("Sending packet " + new String(packet.deconstruct(), StandardCharsets.UTF_8) + " to " + channel.remoteAddress() , Logger.Level.TRACE);
            channel.writeAndFlush(buffer);
        }
    }

    private void notifyPeerOfDisconnect() {
        try {
            ServerHandler peerHandler = (ServerHandler) Server.connections.keySet().toArray()[0];
            SocketChannel channel = (SocketChannel) Server.connections.values().toArray()[0];

            final ByteBuf toOtherBuffer = channel.alloc().buffer();
            DisconnectPacket packet = new DisconnectPacket();
            toOtherBuffer.writeBytes(peerHandler.connection.encryption.encrypt(packet.deconstruct()));
            Logger.log("Sending packet " + new String(packet.deconstruct(), StandardCharsets.UTF_8) + " to " + channel.remoteAddress() , Logger.Level.TRACE);
            channel.writeAndFlush(toOtherBuffer);
        }catch (ArrayIndexOutOfBoundsException ignored){
            //Last Client disconnected
        }
    }

    /**
     * Executed if an exception is caught
     * @param cause the Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.log(cause, Logger.Level.ERROR);
    }

    public static class ClientConnection {

        private String name;
        private final Channel channel;
        private final EncryptedConnection encryption;

        public ClientConnection(Channel channel, EncryptedConnection encryption){
            this.channel = channel;
            this.encryption = encryption;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}