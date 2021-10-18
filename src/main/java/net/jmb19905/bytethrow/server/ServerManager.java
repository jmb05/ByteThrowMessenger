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

import java.util.*;

/**
 * The Server
 */
public class ServerManager {

    private final Server server;
    private List<AbstractChat> chats = new ArrayList<>();
    private final Map<String, TcpServerHandler> onlineClients = new HashMap<>();

    public ServerManager(int port) {
        this.server = new Server(port);
        TcpServerConnection connection = this.server.getConnection();
        connection.addConnectedEventListener(evt -> {
            TcpServerHandler serverHandler = (TcpServerHandler) evt.getSource();
            TcpServerConnection channelHandler = (TcpServerConnection) serverHandler.getConnection();
            SocketChannel channel = channelHandler.getClientConnections().get(serverHandler);
            Logger.info("Client: \"" + channel.remoteAddress() + "\" is now connected");
        });
        connection.addDisconnectedEventListener(evt -> {
            TcpServerHandler serverHandler = (TcpServerHandler) evt.getSource();
            TcpServerConnection channelHandler = (TcpServerConnection) serverHandler.getConnection();
            SocketChannel channel = channelHandler.getClientConnections().get(serverHandler);
            Logger.info("Client: \"" + channel.remoteAddress() + "\" is now disconnected");
            if (!channelHandler.isClosed()) {
                Optional<String> clientName = onlineClients.keySet()
                        .stream()
                        .filter(name -> onlineClients.get(name).equals(serverHandler))
                        .findFirst();
                clientName.ifPresentOrElse(s -> notifyPeersOfDisconnect(s, serverHandler), () -> Logger.warn("No such client: " + clientName));
            }
            server.getConnection().getClientConnections().remove(serverHandler);
        });
        connection.addErrorEventListener(evt -> {
            TcpServerHandler serverHandler = (TcpServerHandler) evt.getSource();
            TcpServerConnection channelHandler = (TcpServerConnection) serverHandler.getConnection();
            SocketChannel channel = channelHandler.getClientConnections().get(serverHandler);

            FailPacket failPacket = new FailPacket();
            failPacket.cause = "internal";
            failPacket.message = "internal_error";
            failPacket.extra = "";
            NetworkingUtility.sendPacket(failPacket, channel, serverHandler.getEncryption());
        });
    }

    public void start() {
        this.server.start();
    }

    public void addOnlineClient(String client, TcpServerHandler handler) {
        onlineClients.put(client, handler);
    }

    public void removeOnlineClient(String client) {
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

    public PeerChat getChat(String user1, String user2) {
        for (AbstractChat chat : chats) {
            if (chat instanceof PeerChat) {
                List<String> users = chat.getMembers();
                if (users.contains(user1) && users.contains(user2)) {
                    return ((PeerChat) chat);
                }
            }
        }
        return null;
    }

    public List<AbstractChat> getChats(String user) {
        List<AbstractChat> chatsContainingUser = new ArrayList<>();
        for (AbstractChat chat : chats) {
            List<String> users = chat.getMembers();
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

    public boolean isClientOnline(String name) {
        return onlineClients.get(name) != null;
    }

    public Map<String, TcpServerHandler> getOnlineClients() {
        return onlineClients;
    }

    public void changeName(String oldName, String newName) {
        for (AbstractChat chat : chats) {
            List<String> chatParticipants = chat.getMembers();
            chatParticipants.remove(oldName);
            chatParticipants.add(newName);
            chat.setMembers(chatParticipants);
        }
    }

    public String[] getPeerNames(String clientName) {
        List<String> names = new ArrayList<>();
        for (AbstractChat chat : chats) {
            List<String> chatParticipants = chat.getMembers();
            if (chatParticipants.contains(clientName)) {
                for (String otherName : chatParticipants) {
                    if (!otherName.equals(clientName)) {
                        names.add(otherName);
                    }
                }
            }
        }
        return names.toArray(new String[0]);
    }

    /**
     * Tell all online peers that the client has now disconnected
     */
    private void notifyPeersOfDisconnect(String disconnectedClientName, TcpServerHandler serverHandler) {
        for (AbstractChat chat : getChats(disconnectedClientName)) {
            List<String> notifiedClients = new ArrayList<>();
            notifiedClients.add(disconnectedClientName);
            for (String clientName : chat.getMembers()) {
                if (!notifiedClients.contains(clientName)) {
                    DisconnectPacket disconnectPacket = new DisconnectPacket();
                    disconnectPacket.name = disconnectedClientName;
                    sendPacketToPeer(clientName, disconnectPacket, serverHandler);
                    notifiedClients.add(clientName);
                }
            }
        }
    }

    /**
     * Sends a packet to the peer of this client
     *
     * @param packet the packet to be sent
     */
    public void sendPacketToPeer(String peerName, Packet packet, TcpServerHandler serverHandler) {
        TcpServerHandler peerHandler = getPeerHandler(peerName, serverHandler);
        if (peerHandler != null) {
            SocketChannel channel = ((TcpServerConnection) serverHandler.getConnection()).getClientConnections().get(peerHandler);
            Logger.trace("Sending packet " + packet + " to " + channel.remoteAddress());
            NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
        } else {
            Logger.warn("Peer: " + peerName + " not online");
        }
    }

    public void sendPacketToGroup(String groupName, Packet packet, TcpServerHandler serverHandler) {
        AbstractChat groupChat = getGroup(groupName);
        groupChat.getMembers().stream().filter(this::isClientOnline).forEach(peer -> {
            TcpServerHandler peerHandler = getPeerHandler(peer, serverHandler);
            if (peerHandler != null) {
                SocketChannel channel = ((TcpServerConnection) serverHandler.getConnection()).getClientConnections().get(peerHandler);
                Logger.trace("Sending packet " + packet + " to " + channel.remoteAddress());
                NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
            }
        });
    }

    /**
     * @return the ServerHandler of the current peer
     */
    public TcpServerHandler getPeerHandler(String peerName, TcpServerHandler ownHandler) {
        for (TcpServerHandler peerHandler : ((TcpServerConnection) ownHandler.getConnection()).getClientConnections().keySet()) {
            if (peerHandler != ownHandler) {
                String currentPeerName = getClientName(peerHandler);
                if (peerName.equals(currentPeerName) && !peerName.isBlank()) {
                    return peerHandler;
                }
            }
        }
        return null;
    }

    public String getClientName(TcpServerHandler handler) {
        Optional<String> clientName = onlineClients.keySet()
                .stream()
                .filter(name -> onlineClients.get(name).equals(handler))
                .findFirst();
        return clientName.orElse("");
    }

    public TcpServerHandler getClientHandler(String name) {
        return onlineClients.get(name);
    }

}
