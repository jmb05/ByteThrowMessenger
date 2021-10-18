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

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.chat.ChatHistorySerialisation;
import net.jmb19905.bytethrow.client.chat.ClientGroupChat;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.packets.LeaveGroupPacket;
import net.jmb19905.bytethrow.common.serial.ChatSerial;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class LeaveGroupPacketHandler extends PacketHandler {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler handler) throws IllegalSideException {
        LeaveGroupPacket leaveGroupPacket = (LeaveGroupPacket) packet;
        ServerManager manager = StartServer.manager;
        String clientName = manager.getClientName(handler);
        if(!clientName.equals(leaveGroupPacket.clientName)) {
            Logger.warn("Invalid LeaveGroupPacket");
            return;
        }
        String groupName = leaveGroupPacket.groupName;
        GroupChat groupChat = manager.getGroup(groupName);
        if(groupChat.removeClient(clientName)) {
            ChatSerial.write(groupChat);
            NetworkingUtility.sendPacket(leaveGroupPacket, ctx.channel(), handler.getEncryption());
            notifyPeers(handler, groupChat, leaveGroupPacket);
        }
    }

    private void notifyPeers(TcpServerHandler serverHandler, GroupChat chat, LeaveGroupPacket packet) {
        ServerManager manager = StartServer.manager;
        for (String peerName : chat.getMembers()) {
            TcpServerHandler peerHandler = manager.getPeerHandler(peerName, serverHandler);
            if (peerHandler != null) {
                SocketChannel channel = ((TcpServerConnection) serverHandler.getConnection()).getClientConnections().get(peerHandler);
                Logger.trace("Sending packet " + packet + " to " + channel.remoteAddress());
                NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
            } else {
                Logger.warn("Peer: " + peerName + " not online");
            }
        }
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet, TcpClientHandler handler) throws IllegalSideException {
        LeaveGroupPacket leaveGroupPacket = (LeaveGroupPacket) packet;
        ClientManager manager = StartClient.manager;
        ClientGroupChat groupChat = manager.getGroup(leaveGroupPacket.groupName);
        if(!manager.name.equals(leaveGroupPacket.clientName)){
            groupChat.removeClient(leaveGroupPacket.clientName);
        }else {
            manager.removeGroup(groupChat);
        }
    }
}
