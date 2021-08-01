package net.jmb19905.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jmb19905.client.gui.LoginDialog;
import net.jmb19905.client.gui.RegisterDialog;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.InvalidLoginException;
import net.jmb19905.common.packets.*;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.Chat;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.security.spec.InvalidKeySpecException;

/**
 * The client-side Handler for the client-server connection
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * The Encryption to thw Server
     */
    private final EncryptedConnection connection = new EncryptedConnection();

    /**
     * Executes when the Connection to the Server starts
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ClientMain.window.appendLine("Connected to Server");

        KeyExchangePacket packet = new KeyExchangePacket();
        packet.key = connection.getPublicKey().getEncoded();

        final ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(packet.deconstruct());

        Logger.log("Sending packet:" + new String(packet.deconstruct(), StandardCharsets.UTF_8), Logger.Level.TRACE);
        ctx.writeAndFlush(buffer);
    }

    /**
     * Executes when the connection to the server drops
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ClientMain.window.appendLine("Disconnected from Server");
        ClientMain.window.dispose();
    }

    /**
     * Executes when a packet from the server is received
     * @param msg the packet as Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        try {
            Packet packet = getPacket(buffer);
            handlePacket(ctx, packet);
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.FATAL);
            System.exit(-1);
        } catch (InvalidLoginException e) {
            Logger.log(e, "Other User has invalid username", Logger.Level.WARN);
        } catch (IllegalStateException e){
            ClientMain.window.log("IllegalStateException: Unexpected Packet signature", Logger.Level.ERROR);
        }
        finally {
            buffer.release();
        }
    }

    private Packet getPacket(ByteBuf buffer) throws InvalidLoginException {
        byte[] encryptedData = new byte[buffer.readableBytes()];
        buffer.readBytes(encryptedData);
        byte[] data = decryptData(encryptedData);

        return Packet.constructPacket(data);
    }

    private byte[] decryptData(byte[] encryptedData) {
        byte[] data;
        if(connection.isUsable()){
            data = connection.decrypt(encryptedData);
        }else {
            data = encryptedData;
        }
        return data;
    }

    private void handlePacket(ChannelHandlerContext ctx, Packet packet) throws InvalidKeySpecException {
        if(packet instanceof KeyExchangePacket){
            handleKeyExchangePacket(ctx, (KeyExchangePacket) packet);
        }else if(packet instanceof SuccessPacket){
            handleSuccessPacket((SuccessPacket) packet);
        }else if(packet instanceof DisconnectPacket){
            handleDisconnectPacket((DisconnectPacket) packet);
            Logger.log("Received Disconnect", Logger.Level.DEBUG);
        }else if(packet instanceof MessagePacket){
            handleMessagePacket((MessagePacket) packet);
        }else if(packet instanceof FailPacket){
            handleFailPacket(ctx, (FailPacket) packet);
        }else if(packet instanceof ConnectPacket){
            handleConnectPacket(ctx, (ConnectPacket) packet);
        }else if(packet instanceof ChatsPacket){
            //TODO: implement adding of chats
        }
    }

    private void handleKeyExchangePacket(ChannelHandlerContext ctx, KeyExchangePacket packet) throws InvalidKeySpecException {
        activateServerEncryption(packet);
        login(ctx);
    }

    private void handleSuccessPacket(SuccessPacket packet) {
        if(packet.type.equals("login")){
            ClientMain.window.appendLine("Login successful");
        }else if(packet.type.equals("register")){
            ClientMain.window.appendLine("Register successful");
        }
    }

    private void handleDisconnectPacket(DisconnectPacket packet) {
        String peerName = packet.name;
        Chat chat = ClientMain.client.getChat(peerName);
        if(chat != null){
            chat.setActive(false);
            ClientMain.window.setPeerStatus(peerName, false);
            ClientMain.window.appendLine(peerName + " disconnected");
        }else {
            Logger.log("Received invalid DisconnectPacket", Logger.Level.WARN);
        }
    }

    private void handleMessagePacket(MessagePacket packet) {
        String sender = packet.message.sender();
        String receiver = packet.message.receiver();
        String encryptedMessage = packet.message.message();
        if(receiver.equals(ClientMain.client.name)) {
            Chat chat = ClientMain.client.getChat(sender);
            if (chat != null) {
                String decryptedMessage = EncryptionUtility.decryptString(chat.encryption, encryptedMessage);
                chat.addMessage(new Chat.Message(sender, receiver, decryptedMessage));
                ClientMain.window.appendLine("<" + sender + "> " + decryptedMessage);
            }else {
                Logger.log("Received Packet from unknown user", Logger.Level.WARN);
            }
        }else {
            Logger.log("Received Packet destined for someone else", Logger.Level.WARN);
        }
    }

    private void handleFailPacket(ChannelHandlerContext ctx, FailPacket packet) {
        String cause = packet.cause;
        JOptionPane.showMessageDialog(ClientMain.window, packet.message, "", JOptionPane.ERROR_MESSAGE);
        if(cause.equals("login")){
            login(ctx);
        }else if(cause.equals("register")){
            register(ctx);
        }else if(cause.startsWith("connect")){
            String peerName = cause.split(":")[1];
            Chat chat = ClientMain.client.getChat(peerName);
            ClientMain.client.chats.remove(chat);
        }
    }

    private void handleConnectPacket(ChannelHandlerContext ctx, ConnectPacket packet) throws InvalidKeySpecException {
        String peerName = packet.name;
        byte[] encodedPeerKey = packet.key;
        if(ClientMain.client.getChat(peerName) == null && packet.firstConnect) {
            Chat chat = new Chat();
            chat.addClient(ClientMain.client.name);
            chat.addClient(peerName);
            chat.initClient();
            chat.setActive(true);
            ClientMain.client.chats.add(chat);

            ClientMain.window.addPeer(peerName);
            ClientMain.window.setPeerStatus(peerName, true);

            chat.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
            ClientMain.window.appendLine("Connection to " + peerName + " encrypted");

            Logger.log("Starting E2E Encryption to: " + peerName, Logger.Level.INFO);
            ConnectPacket replyPacket = new ConnectPacket();
            replyPacket.name = peerName;
            replyPacket.key = chat.encryption.getPublicKey().getEncoded();
            replyPacket.firstConnect = false;

            ByteBuf keyPackBuffer = ctx.alloc().buffer();
            keyPackBuffer.writeBytes(connection.encrypt(replyPacket.deconstruct()));
            Logger.log("Sending packet ConnectPacket to " + peerName, Logger.Level.TRACE);
            ctx.writeAndFlush(keyPackBuffer);
        }else if(ClientMain.client.getChat(peerName) != null && !packet.firstConnect) {
            Chat chat = ClientMain.client.getChat(peerName);
            chat.setActive(true);
            ClientMain.window.setPeerStatus(peerName, true);

            chat.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
            ClientMain.window.appendLine("Connection to " + peerName + " encrypted");

        }else if(ClientMain.client.getChat(peerName) == null && !packet.firstConnect) {
            Logger.log("What are you even doing with your life?", Logger.Level.WARN);
        }else if(ClientMain.client.getChat(peerName) != null && packet.firstConnect){
            Logger.log("Peer tried create a Chat that already exists", Logger.Level.WARN);
        }
    }

    /**
     * Creates the SharedSecret-Key to the server from the server's PublicKey and the PrivateKey
     * @param packet the packet containing the encoded PublicKey
     * @throws InvalidKeySpecException if the PublicKey of the server is invalid
     */
    private void activateServerEncryption(KeyExchangePacket packet) throws InvalidKeySpecException {
        connection.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
        ClientMain.window.appendLine("Connection to Server encrypted");
    }

    /**
     * Sends a LoginPacket tagged as register with the client's name to the server
     */
    private void register(ChannelHandlerContext ctx){
        RegisterDialog registerDialog = new RegisterDialog(true);
        registerDialog.addConfirmButtonActionListener(l -> {
            ClientMain.client.name = registerDialog.getUsername();

            LoginPacket registerPacket = new LoginPacket(true);
            registerPacket.name = ClientMain.client.name;
            registerPacket.password = registerDialog.getPassword();
            ByteBuf registerBuffer = ctx.alloc().buffer();
            registerBuffer.writeBytes(connection.encrypt(registerPacket.deconstruct()));
            Logger.log("Sending packet:" + new String(registerPacket.deconstruct(), StandardCharsets.UTF_8), Logger.Level.TRACE);
            ctx.writeAndFlush(registerBuffer);
        });
        registerDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientMain.window.dispose();
            }
        });
        registerDialog.addLoginButtonActionListener(l -> login(ctx));
        registerDialog.showDialog();
    }

    /**
     * Sends a LoginPacket with the client's name to the server
     */
    private void login(ChannelHandlerContext ctx) {
        LoginDialog loginDialog = new LoginDialog("", "", "");
        loginDialog.addConfirmButtonActionListener(l -> {
            ClientMain.client.name = loginDialog.getUsername();

            LoginPacket loginPacket = new LoginPacket(false);
            loginPacket.name = ClientMain.client.name;
            loginPacket.password = loginDialog.getPassword();
            ByteBuf loginBuffer = ctx.alloc().buffer();
            loginBuffer.writeBytes(connection.encrypt(loginPacket.deconstruct()));
            Logger.log("Sending packet:" + new String(loginPacket.deconstruct(), StandardCharsets.UTF_8), Logger.Level.TRACE);
            ctx.writeAndFlush(loginBuffer);
        });
        loginDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientMain.window.dispose();
            }
        });
        loginDialog.addRegisterButtonActionListener(l -> register(ctx));
        loginDialog.showDialog();
    }

    /**
     * Executed if an exception is caught
     * @param cause the Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.log(cause, Logger.Level.ERROR);
        ctx.close();
    }

    public EncryptedConnection getConnection() {
        return connection;
    }
}
