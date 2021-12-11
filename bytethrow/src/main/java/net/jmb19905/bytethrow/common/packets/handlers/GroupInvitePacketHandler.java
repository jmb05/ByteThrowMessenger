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
import net.jmb19905.bytethrow.common.chat.AbstractChat;
import net.jmb19905.bytethrow.common.chat.client.ClientGroupChat;
import net.jmb19905.bytethrow.common.packets.AddGroupMemberPacket;
import net.jmb19905.bytethrow.common.packets.GroupInvitePacket;
import net.jmb19905.bytethrow.common.serial.ChatSerial;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class GroupInvitePacketHandler extends PacketHandler {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet) throws IllegalSideException {
        GroupInvitePacket groupInvitePacket = (GroupInvitePacket) packet;
        ServerManager manager = StartServer.manager;

        User member = manager.getClient((TcpServerHandler) ctx.handler());

        AbstractChat chat = manager.getGroup(groupInvitePacket.groupName);
        chat.addClient(member);
        ChatSerial.write(chat);

        AddGroupMemberPacket addGroupMemberPacket = new AddGroupMemberPacket();
        addGroupMemberPacket.groupName = groupInvitePacket.groupName;
        addGroupMemberPacket.member = member;

        chat.getMembers().stream().filter(u -> !u.equals(member)).forEach(u -> {
            TcpServerHandler otherHandler = manager.getClientHandler(u);
            SocketChannel channel = manager.getConnection().getClientConnections().get(otherHandler);
            NetworkingUtility.sendPacket(addGroupMemberPacket, channel, otherHandler.getEncryption());
        });
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet) throws IllegalSideException {
        GroupInvitePacket groupInvitePacket = (GroupInvitePacket) packet;
        ClientManager manager = StartClient.manager;

        ClientGroupChat chat = new ClientGroupChat(groupInvitePacket.groupName);
        chat.setMembers(new ArrayList<>(Arrays.asList(groupInvitePacket.members)));

        manager.addGroup(chat);
        Logger.debug("Adding Group (GroupInvitePacketHandler): " + chat.getName());

        NetworkingUtility.sendPacket(packet, ctx.channel(), ((TcpClientHandler) ctx.handler()).getEncryption());
    }
}
