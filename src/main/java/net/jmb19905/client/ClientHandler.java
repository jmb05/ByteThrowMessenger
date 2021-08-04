package net.jmb19905.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jmb19905.client.gui.LoginDialog;
import net.jmb19905.client.gui.RegisterDialog;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalPacketSignatureException;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.exception.InvalidLoginException;
import net.jmb19905.common.packets.KeyExchangePacket;
import net.jmb19905.common.packets.LoginPacket;
import net.jmb19905.common.packets.Packet;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;

/**
 * The client-side Handler for the client-server connection
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * The Encryption to thw Server
     */
    private final EncryptedConnection encryption = new EncryptedConnection();

    private ClientPacketHandler packetHandler;

    /**
     * Executes when the Connection to the Server starts
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ClientMain.window.appendLine("Connected to Server");

        KeyExchangePacket packet = new KeyExchangePacket();
        packet.key = encryption.getPublicKey().getEncoded();

        Logger.log("Sending packet:" + packet, Logger.Level.TRACE);
        NetworkingUtility.sendPacket(packet, ctx.channel(), null);

        this.packetHandler = new ClientPacketHandler(this);
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
        packetHandler.handlePacket(ctx, (Packet) msg);
    }

    /**
     * Sends a LoginPacket tagged as register with the client's name to the server
     */
    public static void register(Channel channel, EncryptedConnection encryption){
        RegisterDialog registerDialog = new RegisterDialog(true);
        registerDialog.addConfirmButtonActionListener(l -> {
            ClientMain.client.name = registerDialog.getUsername();

            LoginPacket registerPacket = new LoginPacket(true);
            registerPacket.name = ClientMain.client.name;
            registerPacket.password = registerDialog.getPassword();
            Logger.log("Sending RegisterPacket", Logger.Level.TRACE);

            NetworkingUtility.sendPacket(registerPacket, channel, encryption);
        });
        registerDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientMain.window.dispose();
                ClientMain.client.stop();
            }
        });
        registerDialog.addLoginButtonActionListener(l -> login(channel, encryption));
        registerDialog.showDialog();
    }

    /**
     * Sends a LoginPacket with the client's name to the server
     */
    public static void login(Channel channel, EncryptedConnection encryption) {
        LoginDialog loginDialog = new LoginDialog("", "", "");
        loginDialog.addConfirmButtonActionListener(l -> {
            ClientMain.client.name = loginDialog.getUsername();

            LoginPacket loginPacket = new LoginPacket(false);
            loginPacket.name = ClientMain.client.name;
            loginPacket.password = loginDialog.getPassword();
            Logger.log("Sending LoginPacket", Logger.Level.TRACE);

            NetworkingUtility.sendPacket(loginPacket, channel, encryption);
        });
        loginDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientMain.window.dispose();
                ClientMain.client.stop();
            }
        });
        loginDialog.addRegisterButtonActionListener(l -> register(channel, encryption));
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

    public EncryptedConnection getEncryption() {
        return encryption;
    }
}
