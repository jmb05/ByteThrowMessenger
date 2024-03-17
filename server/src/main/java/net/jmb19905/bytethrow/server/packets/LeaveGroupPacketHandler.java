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

package net.jmb19905.bytethrow.server.packets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.packets.LeaveGroupPacket;
import net.jmb19905.bytethrow.common.serial.ChatSerial;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;

import java.net.SocketAddress;

public class LeaveGroupPacketHandler implements PacketHandler<LeaveGroupPacket> {
    @Override
    public void handle(HandlingContext ctx, LeaveGroupPacket packet) {
        ServerManager manager = StartServer.manager;
        User client = manager.getClient(ctx.getRemote());
        if(!client.equals(packet.client)) {
            Logger.warn("Invalid LeaveGroupPacket");
            return;
        }
        String groupName = packet.groupName;
        GroupChat groupChat = manager.getGroup(groupName);
        if(groupChat != null) {
            groupChat.removeClient(client);
            ChatSerial.write(groupChat);
            notifyPeers(ctx.getRemote(), groupChat, packet);
        }
        NetworkingUtility.sendPacket(packet, ctx);
    }

    private void notifyPeers(SocketAddress address, GroupChat chat, LeaveGroupPacket packet) {
        ServerManager manager = StartServer.manager;
        for (User peer : chat.getMembers()) {
            manager.sendPacketToPeer(peer, packet, address);
        }
    }
}
