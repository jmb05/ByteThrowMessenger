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
import net.jmb19905.bytethrow.common.packets.AddGroupMemberPacket;
import net.jmb19905.bytethrow.common.packets.GroupInvitePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;

public class AddGroupMemberPacketHandler extends PacketHandler<AddGroupMemberPacket> {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, AddGroupMemberPacket packet) throws IllegalSideException {
        ServerManager manager = StartServer.manager;
        GroupChat groupChat = manager.getGroup(packet.groupName);

        TcpServerHandler memberHandler = manager.getClientHandler(packet.member);
        if (memberHandler != null) {
            GroupInvitePacket groupInvitePacket = new GroupInvitePacket();
            groupInvitePacket.groupName = packet.groupName;
            groupInvitePacket.members = groupChat.getMembers().toArray(new User[0]);

            SocketChannel channel = manager.getConnection().getClientConnections().get(memberHandler);
            NetworkingUtility.sendPacket(groupInvitePacket, channel, memberHandler.getEncryption());
            groupChat.finishInitialization();
        } else {
            NetworkingUtility.sendFail(ctx, "group_add:" + packet.groupName + ":" + packet.member, "not_online", packet.member.getUsername());
        }
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, AddGroupMemberPacket packet) throws IllegalSideException {
        ClientManager manager = StartClient.manager;
        ClientGroupChat groupChat = manager.getGroup(packet.groupName);
        groupChat.addClient(packet.member);
    }
}
