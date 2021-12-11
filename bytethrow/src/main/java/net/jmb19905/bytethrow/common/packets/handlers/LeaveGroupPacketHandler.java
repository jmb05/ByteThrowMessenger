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
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.chat.client.ClientGroupChat;
import net.jmb19905.bytethrow.common.packets.LeaveGroupPacket;
import net.jmb19905.bytethrow.common.serial.ChatSerial;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class LeaveGroupPacketHandler extends PacketHandler {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet) throws IllegalSideException {
        LeaveGroupPacket leaveGroupPacket = (LeaveGroupPacket) packet;
        ServerManager manager = StartServer.manager;
        User client = manager.getClient((TcpServerHandler) ctx.handler());
        if(!client.equals(leaveGroupPacket.client)) {
            Logger.warn("Invalid LeaveGroupPacket");
            return;
        }
        String groupName = leaveGroupPacket.groupName;
        GroupChat groupChat = manager.getGroup(groupName);
        if(groupChat.removeClient(client)) {
            ChatSerial.write(groupChat);
            NetworkingUtility.sendPacket(leaveGroupPacket, ctx);
            notifyPeers((TcpServerHandler) ctx.handler(), groupChat, leaveGroupPacket);
        }
    }

    private void notifyPeers(TcpServerHandler serverHandler, GroupChat chat, LeaveGroupPacket packet) {
        ServerManager manager = StartServer.manager;
        for (User peer : chat.getMembers()) {
            TcpServerHandler peerHandler = manager.getPeerHandler(peer, serverHandler);
            if (peerHandler != null) {
                SocketChannel channel = manager.getConnection().getClientConnections().get(peerHandler);
                Logger.trace("Sending packet " + packet + " to " + channel.remoteAddress());
                NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
            } else {
                Logger.warn("Peer: " + peer.getUsername() + " not online");
            }
        }
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet) throws IllegalSideException {
        LeaveGroupPacket leaveGroupPacket = (LeaveGroupPacket) packet;
        ClientManager manager = StartClient.manager;
        ClientGroupChat groupChat = manager.getGroup(leaveGroupPacket.groupName);
        if(!manager.user.equals(leaveGroupPacket.client)){
            groupChat.removeClient(leaveGroupPacket.client);
        }else {
            manager.removeGroup(groupChat);
        }
    }
}
