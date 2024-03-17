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

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.packets.AddGroupMemberPacket;
import net.jmb19905.bytethrow.common.packets.GroupInvitePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;

import java.net.SocketAddress;

public class AddGroupMemberPacketHandler implements PacketHandler<AddGroupMemberPacket> {
    @Override
    public void handle(HandlingContext ctx, AddGroupMemberPacket packet) {
        ServerManager manager = StartServer.manager;
        GroupChat groupChat = manager.getGroup(packet.groupName);

        SocketAddress memberAddress = manager.getClientAddress(packet.member);
        if (memberAddress != null) {
            GroupInvitePacket groupInvitePacket = new GroupInvitePacket();
            groupInvitePacket.groupName = packet.groupName;
            groupInvitePacket.members = groupChat.getMembers().toArray(new User[0]);

            net.jmb19905.net.NetworkingUtility.send(manager.getNetThread(), memberAddress, groupInvitePacket);
            groupChat.finishInitialization();
        } else {
            NetworkingUtility.sendFail(ctx, "group_add:" + packet.groupName + ":" + packet.member, "not_online", packet.member.getUsername());
        }
    }
}
