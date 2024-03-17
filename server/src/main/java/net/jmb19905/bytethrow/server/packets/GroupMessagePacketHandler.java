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
import net.jmb19905.bytethrow.common.chat.GroupMessage;
import net.jmb19905.bytethrow.common.packets.GroupMessagePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;

import java.net.SocketAddress;

public class GroupMessagePacketHandler implements PacketHandler<GroupMessagePacket> {
    @Override
    public void handle(HandlingContext ctx, GroupMessagePacket packet) {
        GroupMessage message = packet.message;
        ServerManager manager = StartServer.manager;
        SocketAddress address = ctx.getRemote();
        User user = manager.getClient(address);
        if (user.equals(message.getSender())) {
            if (user != null) {
                String groupName = message.getGroupName();
                GroupChat chat = manager.getGroup(groupName);
                if (chat != null) {
                    manager.sendPacketToGroup(groupName, packet, address);
                    Logger.trace("Sent message to group: " + groupName);
                } else {
                    NetworkingUtility.sendFail(ctx, "message", "no_such_chat", groupName);
                }
            } else {
                Logger.warn("Client is trying to communicate but isn't logged in!");
            }
        } else {
            Logger.warn("Received Message with wrong Sender! (" + user.getUsername() + " != " + message.getSender() + ")");
        }
    }
}
