package net.jmb19905.bytethrow.client.networking;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.gui.LoginDialog;
import net.jmb19905.bytethrow.client.gui.RegisterDialog;
import net.jmb19905.bytethrow.client.util.UserDataUtility;
import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.packets.*;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.client.Client;
import net.jmb19905.jmbnetty.client.tcp.TcpClientConnection;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.ConnectException;
import java.util.Timer;
import java.util.*;

/**
 * The Client
 */
public class ClientManager {

    private final Client client;

    public String name = "";

    public List<Chat> chats = new ArrayList<>();

    public SuccessPacket confirmIdentityPacket = null;
    public boolean securePasswords = true;
    private boolean identityConfirmed = false;
    public boolean loggedIn = false;

    public ClientManager(String host, int port){
        this.client = new Client(port, host);
        this.client.getConnection().addConnectedEventListener(evt -> {
            TcpClientHandler channelHandler = (TcpClientHandler) evt.getSource();
            TcpClientConnection clientConnection = (TcpClientConnection) channelHandler.getConnection();
            SocketChannel channel = clientConnection.getChannel();

            StartClient.window.appendLine("Connected to Server");
            Logger.log("Server address is: " + channel.remoteAddress(), Logger.Level.INFO);

            KeyExchangePacket packet = new KeyExchangePacket();
            packet.version = StartClient.version.toString();
            packet.key = clientConnection.getEncryption().getPublicKey().getEncoded();

            Logger.log("Sending packet:" + packet, Logger.Level.TRACE);
            NetworkingUtility.sendPacket(packet, channel, null);
        });
        this.client.getConnection().addDisconnectedEventListener(evt -> ShutdownManager.shutdown(0));
        this.client.getConnection().addErrorEventListener(evt -> {
            TcpClientHandler channelHandler = (TcpClientHandler) evt.getSource();
            TcpClientConnection clientConnection = (TcpClientConnection) channelHandler.getConnection();
            SocketChannel channel = clientConnection.getChannel();

            Logger.log(evt.getCause(), Logger.Level.ERROR);
            channel.close();
        });
    }

    /**
     * Starts the Client
     */
    public void start() throws ConnectException {
        this.client.start();
    }

    public void stop(){
        this.client.stop();
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

        NetworkingUtility.sendPacket(connectPacket, client.getConnection().getChannel(), client.getConnection().getEncryption());
        Logger.trace("Connecting with peer: " + peerName);
    }

    private Chat createNewChat(String peerName) {
        Chat chat = new Chat();
        chat.initClient();
        chat.addClient(name);
        chat.addClient(peerName);
        chats.add(chat);
        StartClient.window.addPeer(peerName);
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
            NetworkingUtility.sendPacket(packet, client.getConnection().getChannel(), client.getConnection().getEncryption());
            Logger.trace("Sent Message: " + message);
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

    /**
     * Sets the identityConfirmed boolean to false after 5 minutes therefore the User has to verify his identity if
     * he does anything confidential (e.g: change password, change username)
     */
    public void confirmIdentity(){
        identityConfirmed = true;
        Logger.log("Identity now confirmed!", Logger.Level.INFO);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                identityConfirmed = false;
                Logger.log("Identity now unconfirmed!", Logger.Level.INFO);
            }
        };
        timer.schedule(task, new Date(System.currentTimeMillis() + 300000)); //300000 ms == 5 min
    }

    /**
     * Sends a LoginPacket tagged as register with the client's name to the server
     */
    public void register(Channel channel, Encryption encryption){
        RegisterDialog registerDialog = new RegisterDialog(securePasswords);
        registerDialog.addConfirmButtonActionListener(l -> {
            name = registerDialog.getUsername();
            StartClient.window.getAccountSettings().setUsername(name);

            LoginPacket registerPacket = new LoginPacket();
            registerPacket.name = name;
            registerPacket.password = registerDialog.getPassword();
            Logger.log("Sending RegisterPacket", Logger.Level.TRACE);

            NetworkingUtility.sendPacket(registerPacket, channel, encryption);
            ConfigManager.saveClientConfig();
        });
        registerDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ShutdownManager.shutdown(0);
            }
        });
        registerDialog.addLoginButtonActionListener(l -> login(channel, encryption));
        registerDialog.showDialog();
    }

    /**
     * Sends a LoginPacket with the client's name to the server
     */
    public void login(Channel channel, Encryption encryption) {
        if(StartClient.config.autoLogin){
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
            ConfigManager.saveClientConfig();
        });
        loginDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ShutdownManager.shutdown(0);
            }
        });
        loginDialog.addRegisterButtonActionListener(l -> register(channel, encryption));
        loginDialog.setVisible(true);
    }

    private void sendLoginPacket(Channel channel, Encryption encryption, String username, String password){
        name = username;
        StartClient.window.getAccountSettings().setUsername(name);

        LoginPacket loginPacket = new LoginPacket();
        loginPacket.name = name;
        loginPacket.password = password;
        Logger.trace("Sending LoginPacket");

        NetworkingUtility.sendPacket(loginPacket, channel, encryption);
    }

    public boolean isIdentityConfirmed(){
        return identityConfirmed;
    }

    public SocketChannel getChannel(){
        return client.getConnection().getChannel();
    }

    public TcpClientHandler getHandler(){
        return client.getConnection().getClientHandler();
    }

}
