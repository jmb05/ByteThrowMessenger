/*
 * A simple Messenger written in Java
 * Copyright (C) 2020-2021  Jared M. Bennett
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.jmb19905.bytethrow.server;

import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.AbstractChat;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.packets.DisconnectPacket;
import net.jmb19905.bytethrow.common.packets.FailPacket;
import net.jmb19905.bytethrow.common.serial.ChatSerial;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.Server;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Server
 */
public class ServerManager {

    private final Server server;
    private List<AbstractChat> chats = new ArrayList<>();
    private final Map<User, TcpServerHandler> onlineClients = new HashMap<>();

    public ServerManager(int port) {
        this.server = new Server(port);
        server.addConnectedEventListener(evt -> {
            TcpServerConnection connection = (TcpServerConnection) evt.getContext().getConnection();
            TcpServerHandler handler = (TcpServerHandler) evt.getContext().getHandler();
            SocketChannel channel = connection.getClientConnections().get(handler);
            Logger.info("Client: \"" + channel.remoteAddress() + "\" is now connected");
        });
        server.addDisconnectedEventListener(evt -> {
            TcpServerConnection connection = (TcpServerConnection) evt.getContext().getConnection();
            TcpServerHandler handler = (TcpServerHandler) evt.getContext().getHandler();
            SocketChannel channel = connection.getClientConnections().get(handler);
            Logger.info("Client: \"" + channel.remoteAddress() + "\" is now disconnected");
            if (!connection.isClosed()) {
                Optional<User> user = onlineClients.keySet()
                        .stream()
                        .filter(u -> onlineClients.get(u).equals(handler))
                        .findFirst();
                user.ifPresent(u -> notifyPeersOfDisconnect(u, connection, handler));
            }
            server.removeServerHandler(handler);
        });
        server.addErrorEventListener(evt -> {
            TcpServerConnection connection = (TcpServerConnection) evt.getContext().getConnection();
            TcpServerHandler handler = (TcpServerHandler) evt.getContext().getHandler();
            SocketChannel channel = connection.getClientConnections().get(handler);

            FailPacket failPacket = new FailPacket();
            failPacket.cause = "internal";
            failPacket.message = "internal_error";
            failPacket.extra = "";
            NetworkingUtility.sendPacket(failPacket, channel, handler.getEncryption());
        });
    }

    public void start() {
        this.server.start();
    }

    public void addOnlineClient(User client, TcpServerHandler handler) {
        onlineClients.put(client, handler);
    }

    public void removeOnlineClient(User client) {
        onlineClients.remove(client);
    }

    public void setChats(List<AbstractChat> chats) {
        this.chats = chats;
        chats.forEach(ChatSerial::write);
    }

    public void addChat(AbstractChat chat) {
        if (!chats.contains(chat)) {
            Logger.debug("Adding Chat: " + chat);
            chats.add(chat);
            ChatSerial.write(chat);
        }
    }

    public void removeChat(AbstractChat chat) {
        chats.remove(chat);
        ChatSerial.deleteChatFile(chat);
    }

    public PeerChat getChat(User user1, User user2) {
        for (AbstractChat chat : chats) {
            if (chat instanceof PeerChat) {
                List<User> users = chat.getMembers();
                if (users.contains(user1) && users.contains(user2)) {
                    return ((PeerChat) chat);
                }
            }
        }
        return null;
    }

    public List<AbstractChat> getChats(User user) {
        List<AbstractChat> chatsContainingUser = new ArrayList<>();
        for (AbstractChat chat : chats) {
            List<User> users = chat.getMembers();
            if (users.contains(user)) {
                chatsContainingUser.add(chat);
            }
        }
        return chatsContainingUser;
    }

    public GroupChat getGroup(String name) {
        return (GroupChat) chats.stream().filter(chat -> chat instanceof GroupChat).filter(chat -> ((GroupChat) chat).getName().equals(name)).findFirst().orElse(null);
    }

    public List<AbstractChat> getChats() {
        return chats;
    }

    public boolean isClientOnline(User user) {
        AtomicBoolean b = new AtomicBoolean(false);
        onlineClients.keySet().stream().filter(u -> u.getUsername().equals(user.getUsername())).findFirst().ifPresent(u -> b.set(true));
        return b.get();
    }

    public Map<User, TcpServerHandler> getOnlineClients() {
        return onlineClients;
    }

    public void changeName(User user, String newName) {
        for (AbstractChat chat : chats) {
            List<User> chatParticipants = chat.getMembers();
            int index = chatParticipants.indexOf(user);
            user.setUsername(newName);
            chatParticipants.set(index, user);
            chat.setMembers(chatParticipants);
        }
    }

    public User[] getPeers(User client) {
        List<User> users = new ArrayList<>();
        for (AbstractChat chat : chats) {
            List<User> chatParticipants = chat.getMembers();
            if (chatParticipants.contains(client)) {
                for (User other : chatParticipants) {
                    if (!other.equals(client)) {
                        users.add(other);
                    }
                }
            }
        }
        return users.toArray(new User[0]);
    }

    /**
     * Tell all online peers that the client has now disconnected
     */
    private void notifyPeersOfDisconnect(User disconnectedClient, TcpServerConnection connection, TcpServerHandler serverHandler) {
        for (AbstractChat chat : getChats(disconnectedClient)) {
            List<User> notifiedClients = new ArrayList<>();
            notifiedClients.add(disconnectedClient);
            for (User client : chat.getMembers()) {
                if (!notifiedClients.contains(client)) {
                    DisconnectPacket disconnectPacket = new DisconnectPacket();
                    disconnectPacket.user = disconnectedClient;
                    sendPacketToPeer(client, disconnectPacket, connection, serverHandler);
                    notifiedClients.add(client);
                }
            }
        }
    }

    /**
     * Sends a packet to the peer of this client
     *
     * @param packet the packet to be sent
     */
    public void sendPacketToPeer(User peer, Packet packet, TcpServerConnection connection, TcpServerHandler serverHandler) {
        TcpServerHandler peerHandler = getPeerHandler(peer, serverHandler);
        if (peerHandler != null) {
            SocketChannel channel = connection.getClientConnections().get(peerHandler);
            Logger.trace("Sending packet " + packet + " to " + channel.remoteAddress());
            NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
        } else {
            Logger.warn("Peer: " + peer.getUsername() + " not online");
        }
    }

    public void sendPacketToGroup(String groupName, Packet packet, TcpServerConnection connection, TcpServerHandler serverHandler) {
        AbstractChat groupChat = getGroup(groupName);
        groupChat.getMembers().stream().filter(this::isClientOnline).forEach(peer -> {
            TcpServerHandler peerHandler = getPeerHandler(peer, serverHandler);
            if (peerHandler != null) {
                SocketChannel channel = connection.getClientConnections().get(peerHandler);
                Logger.trace("Sending packet " + packet + " to " + channel.remoteAddress());
                NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
            }
        });
    }

    /**
     * @return the ServerHandler of the current peer
     */
    public TcpServerHandler getPeerHandler(User peer, TcpServerHandler ownHandler) {
        for (TcpServerHandler peerHandler : getConnection().getClientConnections().keySet()) {
            if (peerHandler != ownHandler) {
                User currentPeer = getClient(peerHandler);
                if (peer.equals(currentPeer) && peer != null) {
                    return peerHandler;
                }
            }
        }
        return null;
    }

    @Nullable
    public User getClient(TcpServerHandler handler) {
        Optional<User> clientName = onlineClients.keySet()
                .stream()
                .filter(u -> onlineClients.get(u).equals(handler))
                .findFirst();
        return clientName.orElse(null);
    }

    public TcpServerConnection getConnection() {
        return server.getConnection();
    }

    public TcpServerHandler getClientHandler(User user) {
        AtomicReference<TcpServerHandler> handler = new AtomicReference<>();
        String username = user.getUsername();
        onlineClients.keySet().stream().filter(u -> u.getUsername().equals(username)).findFirst().ifPresent(us -> handler.set(onlineClients.get(us)));
        return handler.get();
    }

}
