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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.client.gui.CreateGroupDialog;
import net.jmb19905.bytethrow.client.gui.LoginDialog;
import net.jmb19905.bytethrow.client.gui.RegisterDialog;
import net.jmb19905.bytethrow.client.util.UserDataUtility;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.*;
import net.jmb19905.bytethrow.common.chat.client.ChatHistorySerialisation;
import net.jmb19905.bytethrow.common.chat.client.ClientGroupChat;
import net.jmb19905.bytethrow.common.chat.client.ClientPeerChat;
import net.jmb19905.bytethrow.common.chat.client.IClientChat;
import net.jmb19905.bytethrow.common.packets.*;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.client.Client;
import net.jmb19905.jmbnetty.client.tcp.TcpClientConnection;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.handler.AbstractChannelHandler;
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

    public User user = new User("");

    private final List<IClientChat<? extends Message>> chats = new ArrayList<>();

    public SuccessPacket confirmIdentityPacket = null;
    public boolean securePasswords = true;
    private boolean identityConfirmed = false;
    public boolean loggedIn = false;

    public ClientManager(String host, int port) {
        this.client = new Client(port, host);
        this.client.addConnectedEventListener(evt -> {
            TcpClientConnection clientConnection = (TcpClientConnection) evt.getSource();
            SocketChannel channel = clientConnection.getChannel();

            Logger.info("Server address is: " + channel.remoteAddress());

            HandshakePacket packet = new HandshakePacket();
            packet.version = StartClient.version.toString();
            packet.key = clientConnection.getClientHandler().getEncryption().getPublicKey().getEncoded();

            Logger.trace("Sending packet: " + packet);
            NetworkingUtility.sendPacket(packet, channel, null);
        });
        this.client.addDisconnectedEventListener(evt -> ShutdownManager.shutdown(0));
        this.client.addErrorEventListener(evt -> {
            TcpClientConnection clientConnection = (TcpClientConnection) evt.getSource();
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

    public void connectToPeer(User peer) {
        if (getChat(peer) != null) {
            JOptionPane.showMessageDialog(null, "You have already started a conversation with this peer.", "", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PeerChat chat = createNewChat(peer);

        ConnectPacket connectPacket = new ConnectPacket();
        connectPacket.user = peer;
        connectPacket.key = chat.getEncryption().getPublicKey().getEncoded();
        connectPacket.connectType = ConnectPacket.ConnectType.FIRST_CONNECT;

        client.send(connectPacket);
        Logger.trace("Connecting with peer: " + peer.getUsername());
    }

    private ClientPeerChat createNewChat(User peer) {
        ClientPeerChat chat = new ClientPeerChat(peer);
        chat.initClient();
        addChat(chat);
        return chat;
    }

    public void addChat(ClientPeerChat chat) {
        chats.add(chat);
        ChatHistorySerialisation.saveChat(user, chat);
        Logger.debug("Added Peer: " + chat.getOther(user).getUsername());
        StartClient.guiManager.addPeer(chat);
    }

    public void removeChat(ClientPeerChat chat) {
        Logger.debug("Removing Chat: " + chat);
        chats.remove(chat);
        ChatHistorySerialisation.deleteHistory(user.getUsername(), chat);
        StartClient.guiManager.removeChat(chat);
    }

    public void addGroup(ClientGroupChat chat) {
        if (chat.getName() == null) {
            Logger.warn("Cannot add Group! Has no name!");
            return;
        }
        Logger.debug("Adding Group: " + chat.getName());
        chats.add(chat);
        ChatHistorySerialisation.saveChat(user, chat);
        StartClient.guiManager.addGroup(chat);
    }

    public void removeGroup(ClientGroupChat chat) {
        Logger.debug("Removing Group: " + chat.getName());
        chats.remove(chat);
        ChatHistorySerialisation.deleteHistory(user.getUsername(), chat);
        StartClient.guiManager.removeChat(chat);
    }

    public ClientGroupChat getGroup(String name) {
        for (IClientChat<? extends Message> chat : chats) {
            Logger.debug(chat.toString());
        }
        return (ClientGroupChat) chats.stream().filter(chat -> chat instanceof ClientGroupChat).filter(chat -> ((GroupChat) chat).getName().equals(name)).findFirst().orElse(null);
    }

    public void clearChats() {
        Logger.warn("Clearing Chats");
        chats.clear();
    }

    /**
     * Sends a message to the peer
     *
     * @param message the message as String
     */
    public boolean sendPeerMessage(PeerMessage message) {
        String plainMessage = message.getMessage();
        PeerMessagePacket packet = new PeerMessagePacket();
        ClientPeerChat chat = getChat(message.getReceiver());
        if (chat != null) {
            packet.message = PeerMessage.encrypt(message, chat.getEncryption());
            client.send(packet);
            Logger.trace("Sent Message: " + plainMessage + " as: " + StartClient.manager.user);

            //Save the Packet to History as plain text
            message.setMessage(plainMessage);
            chat.addMessage(message);
            ChatHistorySerialisation.saveChat(user, chat);
            return true;
        }
        return false;
    }

    /**
     * Sends a message to the group
     *
     * @param message the message as String
     */
    public boolean sendGroupMessage(GroupMessage message) {
        GroupMessagePacket packet = new GroupMessagePacket();
        String groupName = message.getGroupName();
        ClientGroupChat chat = getGroup(groupName);
        Logger.debug("Group: " + groupName + " = " + chat);
        if (chat != null) {
            packet.message = message;
            chat.addMessage(packet.message);
            client.send(packet);
            Logger.trace("Sent Message: " + message.getMessage());
            ChatHistorySerialisation.saveChat(user, chat);
            return true;
        }
        return false;
    }

    public ClientPeerChat getChat(User peer) {
        return (ClientPeerChat) chats.stream().filter(chat -> chat instanceof ClientPeerChat).filter(chat -> chat.getMembers().contains(peer)).findFirst().orElse(null);
    }

    /**
     * Sets the identityConfirmed boolean to false after 5 minutes therefore the User has to verify his identity if
     * he does anything confidential (e.g: change password, change username)
     */
    public void confirmIdentity() {
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
    public void register(ChannelHandlerContext ctx) {
        Encryption encryption = ((AbstractChannelHandler) ctx.handler()).getEncryption();
        RegisterDialog.RegisterData registerData = StartClient.guiManager.showRegisterDialog(() -> login(ctx));
        if (registerData != null) {
            UserDataUtility.writeUserFile(registerData.username(), registerData.password(), new File("userdata/user.dat"));
            sendRegisterData(ctx.channel(), encryption, registerData.username(), registerData.password());
        }
        ConfigManager.saveClientConfig();
    }

    private void sendRegisterData(Channel channel, Encryption encryption, String username, String password) {
        user.setUsername(username);
        StartClient.guiManager.setUsername(user.getUsername());

        RegisterPacket registerPacket = new RegisterPacket();
        registerPacket.user = new User(username, password);

        NetworkingUtility.sendPacket(registerPacket, channel, encryption);
    }

    /**
     * Sends a LoginPacket with the client's name to the server
     */
    public void login(ChannelHandlerContext ctx) {
        Encryption encryption = ((AbstractChannelHandler) ctx.handler()).getEncryption();

        if (StartClient.config.autoLogin) {
            String[] data = UserDataUtility.readUserFile(new File("userdata/user.dat"));
            if (data.length == 2) {
                sendLoginPacket(ctx.channel(), encryption, data[0], data[1]);
                return;
            }
        }

        LoginDialog.LoginData loginData = StartClient.guiManager.showLoginDialog(() -> register(ctx));
        if (loginData != null) {
            UserDataUtility.writeUserFile(loginData.username(), loginData.password(), new File("userdata/user.dat"));
            sendLoginPacket(ctx.channel(), encryption, loginData.username(), loginData.password());
        }
        ConfigManager.saveClientConfig();
    }

    public void relogin(ChannelHandlerContext ctx) {
        LoginDialog.LoginData loginData = StartClient.guiManager.showLoginDialog(() -> register(ctx));
        if (loginData != null) {
            UserDataUtility.writeUserFile(loginData.username(), loginData.password(), new File("userdata/user.dat"));
            sendLoginPacket(ctx.channel(), ((AbstractChannelHandler) ctx.handler()).getEncryption(), loginData.username(), loginData.password());
        }
        ConfigManager.saveClientConfig();
    }

    private void sendLoginPacket(Channel channel, Encryption encryption, String username, String password) {
        user.setUsername(username);
        StartClient.guiManager.setUsername(user.getUsername());

        LoginPacket loginPacket = new LoginPacket();
        loginPacket.user = new User(username, password);
        loginPacket.user.setAvatarSeed(user.getAvatarSeed());

        NetworkingUtility.sendPacket(loginPacket, channel, encryption);
    }

    public void createGroup() {
        Thread thread = new Thread(() -> {
            CreateGroupDialog.CreateGroupData data = StartClient.guiManager.showCreateGroup();
            if (data != null) {
                CreateGroupPacket createGroupPacket = new CreateGroupPacket();
                createGroupPacket.groupName = data.groupName();
                client.send(createGroupPacket);

                Arrays.stream(data.members()).filter(Objects::nonNull).filter(member -> !member.equals(user)).forEach(member -> {
                    AddGroupMemberPacket addGroupMemberPacket = new AddGroupMemberPacket();
                    addGroupMemberPacket.groupName = data.groupName();
                    addGroupMemberPacket.member = member;
                    client.send(addGroupMemberPacket);
                });
            }
        });
        thread.start();
    }

    public void disconnectFromPeer(User peer){
        DisconnectPeerPacket packet = new DisconnectPeerPacket();
        packet.peer = peer;
        client.send(packet);
    }

    public void leaveGroup(String groupName){
        LeaveGroupPacket packet = new LeaveGroupPacket();
        packet.client = user;
        packet.groupName = groupName;
        client.send(packet);
    }

    public boolean isIdentityConfirmed() {
        return identityConfirmed;
    }

    public Client getClient() {
        return client;
    }
}
