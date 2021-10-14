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
import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.packets.DisconnectPacket;
import net.jmb19905.bytethrow.common.packets.FailPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.Server;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Server
 */
public class ServerManager {

    private final Server server;
    private List<Chat> chats = new ArrayList<>();
    private final Map<String, TcpServerHandler> onlineClients = new HashMap<>();

    public ServerManager(int port){
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
            server.getConnection().getClientConnections().remove(serverHandler);
            if(!channelHandler.isClosed()) {
                Optional<String> clientName = onlineClients.keySet()
                        .stream()
                        .filter(name -> onlineClients.get(name).equals(serverHandler))
                        .findFirst();
                clientName.ifPresent(s -> notifyPeersOfDisconnect(s, serverHandler));
            }
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

    public void start(){
        this.server.start();
    }

    public void addOnlineClient(String client, TcpServerHandler handler){
        onlineClients.put(client, handler);
    }

    public void removeOnlineClient(String client){
        onlineClients.remove(client);
    }

    public void setChats(List<Chat> chats){
        this.chats = chats;
    }

    public void addChat(Chat chat){
        if(!chats.contains(chat)){
            Logger.debug("Adding Chat: " + chat);
            chats.add(chat);
        }
    }

    public Chat getChats(String user1, String user2){
        for(Chat chat : chats){
            List<String> users = chat.getClients();
            if(users.contains(user1) && users.contains(user2)){
                return chat;
            }
        }
        return null;
    }

    public List<Chat> getChats(String user){
        List<Chat> chatsContainingUser = new ArrayList<>();
        for(Chat chat : chats){
            List<String> users = chat.getClients();
            if(users.contains(user)){
                chatsContainingUser.add(chat);
            }
        }
        return chatsContainingUser;
    }

    public Chat getGroup(String name) {
        AtomicReference<Chat> chatAtomicReference = new AtomicReference<>();
        chats.stream().filter(chat -> chat.getName() != null).filter(chat -> chat.getName().equals(name)).findFirst().ifPresent(chatAtomicReference::set);
        return chatAtomicReference.get();
    }

    public List<Chat> getChats() {
        return chats;
    }

    public boolean isClientOnline(String name){
        return onlineClients.get(name) != null;
    }

    public void changeName(String oldName, String newName){
        for (Chat chat : chats) {
            List<String> chatParticipants = chat.getClients();
            chatParticipants.remove(oldName);
            chatParticipants.add(newName);
            chat.setClients(chatParticipants);
        }
    }

    public String[] getPeerNames(String clientName) {
        List<String> names = new ArrayList<>();
        for (Chat chat : chats) {
            List<String> chatParticipants = chat.getClients();
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
        for(Chat chat : getChats(disconnectedClientName)){
            List<String> clients = chat.getClients();
            for(String clientName : clients){
                if(!clientName.equals(disconnectedClientName)){
                    DisconnectPacket disconnectPacket = new DisconnectPacket();
                    disconnectPacket.name = disconnectedClientName;
                    sendPacketToPeer(clientName, disconnectPacket, serverHandler);
                }
            }
        }
    }

    /**
     * Sends a packet to the peer of this client
     * @param packet the packet to be sent
     */
    public void sendPacketToPeer(String peerName, Packet packet, TcpServerHandler serverHandler){
        TcpServerHandler peerHandler = getPeerHandler(peerName, serverHandler);
        if(peerHandler != null) {
            SocketChannel channel = ((TcpServerConnection) serverHandler.getConnection()).getClientConnections().get(peerHandler);
            Logger.trace("Sending packet " + packet + " to " + channel.remoteAddress());
            NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
        }else {
            Logger.warn("Could not find peer: " + peerName);
        }
    }

    public void sendPacketToGroup(String groupName, Packet packet, TcpServerHandler serverHandler){
        Chat groupChat = getGroup(groupName);
        groupChat.getClients().stream().filter(this::isClientOnline).forEach(peer -> {
            TcpServerHandler peerHandler = getPeerHandler(peer, serverHandler);
            if(peerHandler != null) {
                SocketChannel channel = ((TcpServerConnection) serverHandler.getConnection()).getClientConnections().get(peerHandler);
                Logger.trace("Sending packet " + packet + " to " + channel.remoteAddress());
                NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
            }
        });
    }

    /**
     * @return the ServerHandler of the current peer
     */
    public TcpServerHandler getPeerHandler(String peerName, TcpServerHandler ownHandler){
        for(TcpServerHandler peerHandler : ((TcpServerConnection) ownHandler.getConnection()).getClientConnections().keySet()) {
            if (peerHandler != ownHandler) {
                String currentPeerName = getClientName(peerHandler);
                if (peerName.equals(currentPeerName) && !peerName.isBlank()) {
                    return peerHandler;
                }
            }
        }
        return null;
    }

    public String getClientName(TcpServerHandler handler){
        Optional<String> clientName = onlineClients.keySet()
                .stream()
                .filter(name -> onlineClients.get(name).equals(handler))
                .findFirst();
        return clientName.orElse("");
    }

    public TcpServerHandler getClientHandler(String name){
        return onlineClients.get(name);
    }

}
