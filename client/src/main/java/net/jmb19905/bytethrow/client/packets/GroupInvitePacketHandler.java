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

package net.jmb19905.bytethrow.client.packets;

import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.chat.client.ClientGroupChat;
import net.jmb19905.bytethrow.common.packets.GroupInvitePacket;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class GroupInvitePacketHandler implements PacketHandler<GroupInvitePacket> {

    @Override
    public void handle(HandlingContext ctx, GroupInvitePacket packet) {
        ClientManager manager = StartClient.manager;

        ClientGroupChat chat = new ClientGroupChat(packet.groupName);
        chat.setMembers(new ArrayList<>(Arrays.asList(packet.members)));

        manager.addGroup(chat);
        Logger.debug("Adding Group (GroupInvitePacketHandler): " + chat.getName());

        manager.send(packet);
    }
}
