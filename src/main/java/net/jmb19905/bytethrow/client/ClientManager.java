/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.client.gui.CreateGroupDialog;
import net.jmb19905.bytethrow.client.gui.LoginDialog;
import net.jmb19905.bytethrow.client.gui.RegisterDialog;
import net.jmb19905.bytethrow.client.util.UserDataUtility;
import net.jmb19905.bytethrow.common.chat.*;
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
import java.io.File;
import java.util.Timer;
import java.util.*;

/**
 * The Client
 */
public class ClientManager {

    private final Client client;

    public String name = "";

    private final List<Chat> chats = new ArrayList<>();

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

            StartClient.guiManager.appendLine("Connected to Server");
            Logger.info("Server address is: " + channel.remoteAddress());

            HandshakePacket packet = new HandshakePacket();
            packet.version = StartClient.version.toString();
            packet.key = channelHandler.getEncryption().getPublicKey().getEncoded();

            Logger.trace("Sending packet: " + packet);
            NetworkingUtility.sendPacket(packet, channel, null);
        });
        this.client.getConnection().addDisconnectedEventListener(evt -> ShutdownManager.shutdown(0));
        this.client.getConnection().addErrorEventListener(evt -> {
            TcpClientHandler channelHandler = (TcpClientHandler) evt.getSource();
            TcpClientConnection clientConnection = (TcpClientConnection) channelHandler.getConnection();
            SocketChannel channel = clientConnection.getChannel();

            Logger.error(evt.getCause());
            channel.close();
        });
    }

    /**
     * Starts the Client
     */
    public void start() {
        this.client.start();
    }

    public void connectToPeer(String peerName){
        if(getChat(peerName) != null){
            JOptionPane.showMessageDialog(null, "You have already started a conversation with this peer.", "", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PeerChat chat = createNewChat(peerName);

        ConnectPacket connectPacket = new ConnectPacket();
        connectPacket.name = peerName;
        connectPacket.key = chat.getEncryption().getPublicKey().getEncoded();
        connectPacket.connectType = ConnectPacket.ConnectType.FIRST_CONNECT;

        NetworkingUtility.sendPacket(connectPacket, client.getConnection().getChannel(), client.getConnection().getClientHandler().getEncryption());
        Logger.trace("Connecting with peer: " + peerName);
    }

    private PeerChat createNewChat(String peerName) {
        PeerChat chat = new PeerChat(peerName);
        chat.initClient();
        chats.add(chat);
        Logger.debug("Added Peer");
        StartClient.guiManager.addPeer(peerName);
        return chat;
    }

    public void addChat(PeerChat chat){
        chats.add(chat);
        Logger.debug("Added Peer");
        StartClient.guiManager.addPeer(chat.getOther(name));
    }

    public void removeChat(PeerChat chat){
        Logger.debug("Removing Chat: " + chat);
        chats.remove(chat);
        StartClient.guiManager.removePeer(chat.getOther(name));
    }

    public void addGroup(GroupChat chat){
        if(chat.getName() == null){
            Logger.warn("Cannot add Group! Has no name!");
            return;
        }
        Logger.debug("Adding Group: " + chat.getName());
        chats.add(chat);
        StartClient.guiManager.addGroup(chat.getName());
    }

    public void removeGroup(GroupChat chat){
        Logger.debug("Removing Group: " + chat.getName());
        chats.remove(chat);
        StartClient.guiManager.removeGroup(chat.getName());
    }

    public GroupChat getGroup(String name){
        for(Chat chat : chats){
            Logger.debug(chat.toString());
        }
        return (GroupChat) chats.stream().filter(chat -> chat instanceof GroupChat).filter(chat -> ((GroupChat) chat).getName().equals(name)).findFirst().orElse(null);
    }

    public void clearChats(){
        Logger.warn("Clearing Chats");
        chats.clear();
    }

    /**
     * Sends a message to the peer
     * @param message the message as String
     */
    public boolean sendPeerMessage(String recipient, String message){
        PeerMessagePacket packet = new PeerMessagePacket();
        PeerChat chat = getChat(recipient);
        Logger.debug("Trying to send to Chat: " + chat + " from: " + recipient);
        if(chat != null) {
            String encryptedMessage = EncryptionUtility.encryptString(chat.getEncryption(), message);
            packet.message = new PeerMessage(name, recipient, encryptedMessage);
            NetworkingUtility.sendPacket(packet, client.getConnection().getChannel(), client.getConnection().getClientHandler().getEncryption());
            Logger.trace("Sent Message: " + message);
            return true;
        }
        return false;
    }

    /**
     * Sends a message to the group
     * @param message the message as String
     */
    public boolean sendGroupMessage(String groupName, String message){
        GroupMessagePacket packet = new GroupMessagePacket();
        GroupChat chat = getGroup(groupName);
        Logger.debug("Group: " + groupName + " = " + chat);
        if(chat != null){
            packet.message = new GroupMessage(name, groupName, message);
            NetworkingUtility.sendPacket(packet, client.getConnection().getChannel(), client.getConnection().getClientHandler().getEncryption());
            Logger.trace("Sent Message: " + message);
            return true;
        }
        return false;
    }

    public PeerChat getChat(String peerName){
        return (PeerChat) chats.stream().filter(chat -> chat instanceof PeerChat).filter(chat -> chat.getMembers().contains(peerName)).findFirst().orElse(null);
    }

    /**
     * Sets the identityConfirmed boolean to false after 5 minutes therefore the User has to verify his identity if
     * he does anything confidential (e.g: change password, change username)
     */
    public void confirmIdentity(){
        identityConfirmed = true;
        Logger.info("Identity now confirmed!");
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                identityConfirmed = false;
                Logger.info("Identity now unconfirmed!");
            }
        };
        timer.schedule(task, new Date(System.currentTimeMillis() + 300000)); //300000 ms == 5 min
    }

    /**
     * Sends a LoginPacket tagged as register with the client's name to the server
     */
    public void register(Channel channel, Encryption encryption){
        RegisterDialog.RegisterData registerData = StartClient.guiManager.showRegisterDialog(() -> login(channel, encryption));
        if(registerData != null) {
            UserDataUtility.writeUserFile(registerData.username(), registerData.password(), new File("userdata/user.dat"));
            sendRegisterData(channel, encryption, registerData.username(), registerData.password());
        }
        ConfigManager.saveClientConfig();
    }

    private void sendRegisterData(Channel channel, Encryption encryption, String username, String password){
        name = username;
        StartClient.guiManager.setUsername(name);

        RegisterPacket registerPacket = new RegisterPacket();
        registerPacket.username = name;
        registerPacket.password = password;
        Logger.trace("Sending RegisterPacket");

        ChannelFuture future = NetworkingUtility.sendPacket(registerPacket, channel, encryption);
        future.addListener(l -> Logger.debug("RegisterPacket sent"));
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

        LoginDialog.LoginData loginData = StartClient.guiManager.showLoginDialog(() -> register(channel, encryption));
        if(loginData != null) {
            UserDataUtility.writeUserFile(loginData.username(), loginData.password(), new File("userdata/user.dat"));
            sendLoginPacket(channel, encryption, loginData.username(), loginData.password());
        }
        ConfigManager.saveClientConfig();
    }

    public void relogin(Channel channel, Encryption encryption){
        LoginDialog.LoginData loginData = StartClient.guiManager.showLoginDialog(() -> register(channel, encryption));
        if(loginData != null) {
            UserDataUtility.writeUserFile(loginData.username(), loginData.password(), new File("userdata/user.dat"));
            sendLoginPacket(channel, encryption, loginData.username(), loginData.password());
        }
        ConfigManager.saveClientConfig();
    }

    private void sendLoginPacket(Channel channel, Encryption encryption, String username, String password){
        name = username;
        StartClient.guiManager.setUsername(name);

        LoginPacket loginPacket = new LoginPacket();
        loginPacket.username = name;
        loginPacket.password = password;
        Logger.trace("Sending LoginPacket");

        ChannelFuture future = NetworkingUtility.sendPacket(loginPacket, channel, encryption);
        future.addListener(l -> Logger.debug("LoginPacket sent"));
    }

    public void createGroup(){
        Thread thread = new Thread(() -> {
            CreateGroupDialog.CreateGroupData data = StartClient.guiManager.showCreateGroup();
            if(data != null) {
                CreateGroupPacket createGroupPacket = new CreateGroupPacket();
                createGroupPacket.groupName = data.groupName();
                NetworkingUtility.sendPacket(createGroupPacket, client.getConnection().getChannel(), client.getConnection().getClientHandler().getEncryption());
                Logger.debug("Sent Packet: " + createGroupPacket);

                Arrays.stream(data.members()).filter(member -> !member.isBlank()).filter(member -> !member.equals(name)).forEach(member -> {
                    AddGroupMemberPacket addGroupMemberPacket = new AddGroupMemberPacket();
                    addGroupMemberPacket.groupName = data.groupName();
                    addGroupMemberPacket.member = member;
                    NetworkingUtility.sendPacket(addGroupMemberPacket, client.getConnection().getChannel(), client.getConnection().getClientHandler().getEncryption());
                    Logger.debug("Sent Packet: " + addGroupMemberPacket);
                });
            }
        });
        thread.start();
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
