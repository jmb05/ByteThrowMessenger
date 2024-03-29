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

import io.netty.channel.ChannelHandlerContext;
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
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.net.Client;
import net.jmb19905.net.event.ActiveEventListener;
import net.jmb19905.net.event.ExceptionEventListener;
import net.jmb19905.net.event.InactiveEventListener;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.Packet;
import net.jmb19905.net.tcp.ClientTcpThread;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;
import net.jmb19905.util.config.ConfigManager;
import net.jmb19905.util.crypto.Encryption;

import javax.swing.*;
import java.io.File;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.*;

/**
 * The Client
 */
public class ClientManager {

    private final Client client;
    private final ClientTcpThread netThread;
    private SocketAddress serverAddress = null;

    public User user = new User("");

    private final List<IClientChat<? extends Message>> chats = new ArrayList<>();

    public SuccessPacket confirmIdentityPacket = null;
    public boolean securePasswords = true;
    private boolean identityConfirmed = false;
    public boolean loggedIn = false;

    public ClientManager(String host, int port) {
        this.client = new Client(host);
        this.netThread = (ClientTcpThread) client.addTcp(port);

        this.netThread.addEventListener((ActiveEventListener) evt -> {
            serverAddress = evt.getContext().remoteAddress();
            Logger.info("Server address is: " + evt.getContext().remoteAddress());

            Encryption encryption = new Encryption();
            netThread.setEncryption(encryption);

            HandshakePacket packet = new HandshakePacket();
            packet.version = StartClient.version.toString();
            packet.key = encryption.getPublicKey().getEncoded();

            Logger.trace("Sending packet: " + packet);
            NetworkingUtility.sendPacket(packet, evt.getContext());
        });

        this.netThread.addEventListener((InactiveEventListener) evt -> ShutdownManager.shutdown(0));

        this.netThread.addEventListener((ExceptionEventListener) evt -> {
            Logger.error(evt.getCause());
        });

        //this.client.addConnectionRefusedHandler(() -> StartClient.guiManager.showLocalisedError("no_internet"));
    }

    public void initGuiListeners() {
        StartClient.guiManager.addLoginEventListener(evt -> {
            LoginDialog.LoginData data = evt.getData();
            if(data.resultType() == GUIManager.ResultType.CONFIRM) {
                if (data != null) {
                    UserDataUtility.writeUserFile(data.username(), data.password(), new File("userdata/user.dat"));
                    sendLoginPacket(data.username(), data.password());
                }
                ConfigManager.saveConfigFile(StartClient.config);
            }else if(data.resultType() == GUIManager.ResultType.CANCEL) {
                ShutdownManager.shutdown(0);
            }else {
                register();
            }
        });
        StartClient.guiManager.addRegisterEventListener(evt -> {
            RegisterDialog.RegisterData data = evt.getData();
            if(data.resultType() == GUIManager.ResultType.CONFIRM) {
                if (data != null) {
                    UserDataUtility.writeUserFile(data.username(), data.password(), new File("userdata/user.dat"));
                    sendRegisterData(data.username(), data.password());
                }
                ConfigManager.saveConfigFile(StartClient.config);
            }else if(data.resultType() == GUIManager.ResultType.CANCEL) {
                ShutdownManager.shutdown(0);
            }else {
                login();
            }
        });
        StartClient.guiManager.addSendPeerMessageEventListener(evt -> {
            if (sendPeerMessage(evt.getPeerMessage())) {
                StartClient.guiManager.appendMessage(evt.getPeerMessage(), evt.getChatProfile(), true);
            } else {
                StartClient.guiManager.showLocalisedError("chat_doesnt_exist");
            }
        });
        StartClient.guiManager.addSendGroupMessageEventListener(evt -> {
            if(sendGroupMessage(evt.getGroupMessage())) {
                StartClient.guiManager.appendMessage(evt.getGroupMessage(), evt.getChatProfile(), true);
            } else {
                StartClient.guiManager.showLocalisedError("chat_doesnt_exist");
            }
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

        send(connectPacket);
        Logger.trace("Connecting with peer: " + peer.getUsername());
    }

    private ClientPeerChat createNewChat(User peer) {
        ClientPeerChat chat = new ClientPeerChat(StartClient.manager.user, peer);
        chat.initClient();
        addChat(chat);
        cleanChats();
        return chat;
    }

    public void addChat(ClientPeerChat chat) {
        chats.add(chat);
        ChatHistorySerialisation.saveChat(user, chat);
        Logger.debug("Added Peer: " + chat.getOther(user).getUsername());
        StartClient.guiManager.addPeer(chat);
        cleanChats();
    }

    public void removeChat(ClientPeerChat chat) {
        Logger.debug("Removing Chat: " + chat);
        chats.remove(chat);
        ChatHistorySerialisation.deleteHistory(user.getUsername(), chat);
        StartClient.guiManager.removeChat(chat);
        cleanChats();
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
        cleanChats();
    }

    public void removeGroup(ClientGroupChat chat) {
        Logger.debug("Removing Group: " + chat.getName());
        chats.remove(chat);
        ChatHistorySerialisation.deleteHistory(user.getUsername(), chat);
        StartClient.guiManager.removeChat(chat);
        cleanChats();
    }

    public ClientGroupChat getGroup(String name) {
        return (ClientGroupChat) chats.stream().filter(chat -> chat instanceof ClientGroupChat).filter(chat -> ((GroupChat) chat).getName().equals(name)).findFirst().orElse(null);
    }

    public void clearChats() {
        Logger.warn("Clearing Chats");
        chats.clear();
    }

    public void cleanChats() {
        chats.removeIf(chat -> !chat.isValid());
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
            send(packet);
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
            send(packet);
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
    public void register() {
        StartClient.guiManager.showRegisterDialog();
    }

    private void sendRegisterData(String username, String password) {
        user.setUsername(username);
        StartClient.guiManager.setUsername(user.getUsername());

        RegisterPacket registerPacket = new RegisterPacket();
        registerPacket.user = new User(username, password);

        send(registerPacket);
    }

    /**
     * Sends a LoginPacket with the client's name to the server
     */
    public void login() {
        if (StartClient.config.autoLogin) {
            String[] data = UserDataUtility.readUserFile(new File("userdata/user.dat"));
            if (data.length == 2) {
                sendLoginPacket(data[0], data[1]);
                return;
            }
        }

        relogin();
    }

    public void relogin() {
        StartClient.guiManager.showLoginDialog();
    }

    private void sendLoginPacket(String username, String password) {
        user.setUsername(username);
        StartClient.guiManager.setUsername(user.getUsername());

        LoginPacket loginPacket = new LoginPacket();
        loginPacket.user = new User(username, password);
        loginPacket.user.setAvatarSeed(user.getAvatarSeed());

        send(loginPacket);
    }

    public void createGroup() {
        Thread thread = new Thread(() -> {
            CreateGroupDialog.CreateGroupData data = StartClient.guiManager.showCreateGroup();
            if (data != null) {
                CreateGroupPacket createGroupPacket = new CreateGroupPacket();
                createGroupPacket.groupName = data.groupName();
                send(createGroupPacket);

                Arrays.stream(data.members()).filter(Objects::nonNull).filter(member -> !member.equals(user)).forEach(member -> {
                    AddGroupMemberPacket addGroupMemberPacket = new AddGroupMemberPacket();
                    addGroupMemberPacket.groupName = data.groupName();
                    addGroupMemberPacket.member = member;
                    send(addGroupMemberPacket);
                });
            }
        });
        thread.start();
    }

    public void disconnectFromPeer(User peer){
        DisconnectPeerPacket packet = new DisconnectPeerPacket();
        packet.peer = peer;
        send(packet);
    }

    public void leaveGroup(String groupName){
        LeaveGroupPacket packet = new LeaveGroupPacket();
        packet.client = user;
        packet.groupName = groupName;
        send(packet);
    }

    public void send(Packet packet) {
        NetworkingUtility.sendPacket(packet, netThread, serverAddress);
    }

    public boolean isIdentityConfirmed() {
        return identityConfirmed;
    }

    public Client getClient() {
        return client;
    }

    public ClientTcpThread getNetThread() {
        return netThread;
    }
}
