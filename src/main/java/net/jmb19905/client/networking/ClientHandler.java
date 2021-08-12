package net.jmb19905.client.networking;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jmb19905.client.ClientMain;
import net.jmb19905.client.gui.LoginDialog;
import net.jmb19905.client.gui.RegisterDialog;
import net.jmb19905.client.util.UserDataUtility;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.KeyExchangePacket;
import net.jmb19905.common.packets.LoginPacket;
import net.jmb19905.common.packets.Packet;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * The client-side Handler for the client-server connection
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * The Encryption to thw Server
     */
    private final EncryptedConnection encryption = new EncryptedConnection();

    private ClientPacketsHandler packetHandler;

    /**
     * Executes when the Connection to the Server starts
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ClientMain.window.appendLine("Connected to Server");
        Logger.log("Server address is: " + ctx.channel().remoteAddress(), Logger.Level.INFO);

        KeyExchangePacket packet = new KeyExchangePacket();
        packet.version = ClientMain.version.toString();
        packet.key = encryption.getPublicKey().getEncoded();

        Logger.log("Sending packet:" + packet, Logger.Level.TRACE);
        NetworkingUtility.sendPacket(packet, ctx.channel(), null);

        this.packetHandler = new ClientPacketsHandler(this);
    }

    /**
     * Executes when the connection to the server drops
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ClientMain.exit(0, "", true);
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
            ClientMain.window.getAccountSettings().setUsername(ClientMain.client.name);

            LoginPacket registerPacket = new LoginPacket(true);
            registerPacket.name = ClientMain.client.name;
            registerPacket.password = registerDialog.getPassword();
            Logger.log("Sending RegisterPacket", Logger.Level.TRACE);

            NetworkingUtility.sendPacket(registerPacket, channel, encryption);
            ConfigManager.saveClientConfig(ClientMain.config, "config/client_config.json");
        });
        registerDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientMain.exit(0, "", false);
            }
        });
        registerDialog.addLoginButtonActionListener(l -> login(channel, encryption));
        registerDialog.showDialog();
    }

    /**
     * Sends a LoginPacket with the client's name to the server
     */
    public static void login(Channel channel, EncryptedConnection encryption) {
        if(ClientMain.config.autoLogin){
            String[] data = UserDataUtility.readUserFile(new File("userdata/user.dat"));
            if(data.length == 2){
                sendLoginPacket(channel, encryption, data[0], data[1]);
                return;
            }
        }
        LoginDialog loginDialog = new LoginDialog("", "", "", true);
        loginDialog.addConfirmButtonActionListener(l -> {
            UserDataUtility.writeUserFile(loginDialog.getUsername(), loginDialog.getPassword(), new File("userdata/user.dat"));
            sendLoginPacket(channel, encryption, loginDialog.getUsername(), loginDialog.getPassword());
            ConfigManager.saveClientConfig(ClientMain.config, "config/client_config.json");
        });
        loginDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientMain.exit(0, "", false);
            }
        });
        loginDialog.addRegisterButtonActionListener(l -> register(channel, encryption));
        loginDialog.setVisible(true);
    }

    private static void sendLoginPacket(Channel channel, EncryptedConnection encryption, String username, String password){
        ClientMain.client.name = username;
        ClientMain.window.getAccountSettings().setUsername(ClientMain.client.name);

        LoginPacket loginPacket = new LoginPacket(false);
        loginPacket.name = ClientMain.client.name;
        loginPacket.password = password;
        Logger.log("Sending LoginPacket", Logger.Level.TRACE);

        NetworkingUtility.sendPacket(loginPacket, channel, encryption);
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
