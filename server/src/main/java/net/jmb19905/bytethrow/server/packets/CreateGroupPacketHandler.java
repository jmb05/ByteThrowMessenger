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

import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.packets.CreateGroupPacket;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;

public class CreateGroupPacketHandler implements PacketHandler<CreateGroupPacket> {
    @Override
    public void handle(HandlingContext ctx, CreateGroupPacket packet) {
        String groupName = packet.groupName;
        ServerManager manager = StartServer.manager;

        GroupChat chat = new GroupChat(groupName);
        chat.addClient(manager.getClient(ctx.getRemote()));

        manager.addChat(chat);

        ctx.send(packet);
    }
}
