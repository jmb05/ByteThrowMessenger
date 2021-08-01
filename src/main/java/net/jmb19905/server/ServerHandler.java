package net.jmb19905.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.*;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.server.database.SQLiteManager;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

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
        connection = new ClientConnection(new EncryptedConnection());
    }

    /**
     * Executes when the connection to the server drops
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now disconnected", Logger.Level.INFO);
        Server.connections.remove(this);
        notifyPeersOfDisconnect();
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
            Logger.log("Decoded Packet: " + new String(packet.deconstruct(), StandardCharsets.UTF_8), Logger.Level.DEBUG);
            handlePacket(ctx, packet);
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.ERROR);
        } finally {
            buffer.release();
        }
    }

    /**
     * Constructs a Packet from a received ByteBuffer (ByteBuf)
     * @param buffer the received ByteBuffer (ByteBuf)
     * @return a Packet
     */
    private Packet getPacket(ByteBuf buffer) {
        byte[] encryptedData = new byte[buffer.readableBytes()];
        buffer.readBytes(encryptedData);
        byte[] data = decryptData(encryptedData);

        return Packet.constructPacket(data);
    }

    /**
     * Decrypts a byte-array received from the client if connection is already encrypted
     * @param encryptedArray the byte-array that might be encrypted
     * @return a decrypted array of bytes
     */
    private byte[] decryptData(byte[] encryptedArray) {
        byte[] array;
        if(connection.encryption.isUsable()){
            array = connection.encryption.decrypt(encryptedArray);
        }else {
            array = encryptedArray;
        }
        return array;
    }

    /**
     * Handles a constructed incoming Packet
     * @param packet the Packet
     * @throws InvalidKeySpecException if there is an issue with a received key
     */
    private void handlePacket(ChannelHandlerContext ctx, Packet packet) throws InvalidKeySpecException {
        if(packet instanceof KeyExchangePacket){
            handleKeyExchangePacket(ctx, (KeyExchangePacket) packet);
        }else if(packet instanceof LoginPacket){
            handleLoginPacket(ctx, (LoginPacket) packet);
        }else if(packet instanceof ConnectPacket){
            handleConnectPacket(ctx, (ConnectPacket) packet);
        }else if(packet instanceof MessagePacket){
            handleMessagePacket(ctx, (MessagePacket) packet);
        }
    }

    /**
     * Handles a KeyExchangePacket
     * @param packet the KeyExchangePacket
     * @throws InvalidKeySpecException if the Key is invalid
     */
    private void handleKeyExchangePacket(ChannelHandlerContext ctx, KeyExchangePacket packet) throws InvalidKeySpecException {
        byte[] clientEncodedPublicKey = packet.key;

        PublicKey clientPublicKey = EncryptionUtility.createPublicKeyFromData(clientEncodedPublicKey);
        connection.encryption.setReceiverPublicKey(clientPublicKey);

        Logger.log("Connection to Client: " + ctx.channel().remoteAddress() + " is encrypted", Logger.Level.INFO);

        //change the key transferred in the packet to the server's PublicKey so the packet can be reused
        packet.key = connection.encryption.getPublicKey().getEncoded();

        final ByteBuf replyBuffer = ctx.alloc().buffer();
        replyBuffer.writeBytes(packet.deconstruct());
        Logger.log("Sending packet " + new String(packet.deconstruct(), StandardCharsets.UTF_8) + " to " + ctx.channel().remoteAddress() , Logger.Level.TRACE);
        ctx.writeAndFlush(replyBuffer);
    }

    /**
     * Checks if a LoginPacket is actually a Register-Packet and deals with the Packet
     * @param loginPacket the LoginPacket
     */
    private void handleLoginPacket(ChannelHandlerContext ctx, LoginPacket loginPacket) {
        if(loginPacket.getId().equals("register")){
            handleRegister(ctx, loginPacket);
        }else if(loginPacket.getId().equals("login")){
            handleLogin(ctx, loginPacket);
        }
    }

    /**
     * Handles incoming ConnectPackets (used for connection two Clients via a Chat)
     * @param packet the ConnectPacket
     */
    private void handleConnectPacket(ChannelHandlerContext ctx, ConnectPacket packet) {
        String clientName = connection.getName();
        if(!clientName.isBlank()) {
            String peerName = packet.name;
            if (SQLiteManager.hasUser(peerName)) {
                if(Server.isClientOnline(peerName)) {
                    if(Server.getChat(peerName, clientName) == null && packet.firstConnect) {
                        Chat chat = new Chat();
                        chat.addClient(clientName);
                        chat.addClient(peerName);
                        chat.setActive(true);
                        Server.chats.add(chat);

                        packet.name = clientName;
                        sendPacketToPeer(peerName, packet);
                    }else if(Server.getChat(peerName, clientName) != null && !packet.firstConnect) {
                        Chat chat = Server.getChat(peerName, clientName);
                        chat.setActive(true);

                        packet.name = clientName;
                        sendPacketToPeer(peerName, packet);
                    } else if(Server.getChat(peerName, clientName) != null && packet.firstConnect){
                        sendFail(ctx, "connect:" + peerName, "Chat with: " + peerName + " already exists!");
                    } else if(Server.getChat(peerName, clientName) == null && !packet.firstConnect){
                        Logger.log("What are you even doing with your life?", Logger.Level.WARN);
                    }
                }else {
                    sendFail(ctx, "connect:" + peerName, "User: " + peerName + " not online!");
                }
            } else {
                sendFail(ctx, "connect:" + peerName, "No such User: " + peerName);
            }
        }else {
            Logger.log("Client is trying to communicate but isn't logged in!", Logger.Level.WARN);
        }
    }

    /**
     * Handles incoming MessagePackets
     * @param packet the MessagePacket
     */
    private void handleMessagePacket(ChannelHandlerContext ctx, MessagePacket packet) {
        if(connection.getName().equals(packet.message.sender())) {
            String clientName = connection.getName();
            String peerName = packet.message.receiver();
            if (!connection.getName().isBlank()) {
                Chat chat = Server.getChat(clientName, peerName);
                if (chat != null) {
                    if (chat.isActive()) {
                        chat.addMessage(packet.message);
                        forwardMessageToPeer(peerName, packet);
                    } else {
                        sendFail(ctx, "message", "Cannot send Message " + peerName + " is offline!");
                    }
                } else {
                    sendFail(ctx, "message", "Cannot send Message Chat with: " + peerName + " doesn't exist!");
                }
            } else {
                Logger.log("Client is trying to communicate but isn't logged in!", Logger.Level.WARN);
            }
        }else {
            Logger.log("Received Message with wrong Sender!", Logger.Level.WARN);
        }
    }

    /**
     * Checks if a login is valid
     * @param packet the LoginPacket
     */
    private void handleLogin(ChannelHandlerContext ctx, LoginPacket packet) {
        String username = packet.name;
        String password = packet.password;
        if(SQLiteManager.hasUser(username)){
            SQLiteManager.UserData userData = SQLiteManager.getUserByName(username);
            if(BCrypt.checkpw(password, userData.password())){
                handleSuccessfulLogin(ctx, packet);
            }else {
                sendFail(ctx, "login", "Failed to Login! - Wrong password");
            }
        }else {
            sendFail(ctx, "login", "Failed to Login! - User not found");
        }
    }

    /**
     * Checks if a register is valid
     * @param packet the LoginPacket
     */
    private void handleRegister(ChannelHandlerContext ctx, LoginPacket packet) {
        Logger.log("Client is trying to registering", Logger.Level.TRACE);
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

        sendLoginSuccess(ctx, packet); // confirms the login to the current client

        //TODO: send client all names of chats
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
     * Sends message to the current client's peer
     * @param packet the MessagePacket
     */
    private void forwardMessageToPeer(String peerName, MessagePacket packet) {
        sendPacketToPeer(peerName, packet);
        Logger.log("Sent message to recipient: " + peerName, Logger.Level.TRACE);
    }

    /**
     * Sends LoginPacket to client to confirm login
     * @param loginPacket the LoginPacket
     */
    private void sendLoginSuccess(ChannelHandlerContext ctx, LoginPacket loginPacket) {
        SuccessPacket loginSuccessPacket = new SuccessPacket();
        loginSuccessPacket.type = loginPacket.getId();

        ByteBuf replyBuffer = ctx.alloc().buffer();
        replyBuffer.writeBytes(connection.encryption.encrypt(loginSuccessPacket.deconstruct()));
        Logger.log("Sending packet LoginSuccess to " + ctx.channel().remoteAddress() , Logger.Level.TRACE);
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
    private void sendPacketToPeer(String peerName, Packet packet){
        ServerHandler peerHandler = getPeerHandler(peerName);
        SocketChannel channel = Server.connections.get(peerHandler);
        if(peerHandler != null) {
            final ByteBuf buffer = channel.alloc().buffer();
            buffer.writeBytes(peerHandler.connection.encryption.encrypt(packet.deconstruct()));
            Logger.log("Sending packet " + packet + " to " + channel.remoteAddress() , Logger.Level.TRACE);
            channel.writeAndFlush(buffer);
        }
    }

    /**
     * Tell all online peers that the client has now disconnected
     */
    private void notifyPeersOfDisconnect() {
        String disconnectedClientName = connection.getName();
        for(Chat chat : Server.getChat(disconnectedClientName)){
            List<String> clients = chat.getClients();
            for(String clientName : clients){
                if(!clientName.equals(disconnectedClientName)){
                    DisconnectPacket disconnectPacket = new DisconnectPacket();
                    disconnectPacket.name = disconnectedClientName;
                    sendPacketToPeer(clientName, disconnectPacket);
                }
            }
        }
    }

    public ClientConnection getConnection() {
        return connection;
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
        private final EncryptedConnection encryption;

        public ClientConnection(EncryptedConnection encryption){
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