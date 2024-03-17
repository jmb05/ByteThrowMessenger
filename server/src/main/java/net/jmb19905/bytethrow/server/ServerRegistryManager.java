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

import net.jmb19905.bytethrow.common.packets.*;
import net.jmb19905.bytethrow.server.commands.HelpCommand;
import net.jmb19905.bytethrow.server.commands.StopCommand;
import net.jmb19905.bytethrow.server.packets.*;
import net.jmb19905.net.packet.PacketRegistry;
import net.jmb19905.util.commands.CommandRegistry;

public class ServerRegistryManager {

    public static void registerPackets() {
        PacketRegistry.getInstance().register(AddGroupMemberPacket.class, new AddGroupMemberPacketHandler());
        PacketRegistry.getInstance().register(ChangeUserDataPacket.class, new ChangeUserDataPacketHandler());
        PacketRegistry.getInstance().register(ChatsRequestPacket.class, new ChatsRequestPacketHandler());
        PacketRegistry.getInstance().register(CreateGroupPacket.class, new CreateGroupPacketHandler());
        PacketRegistry.getInstance().register(ConnectPacket.class, new ConnectPacketHandler());
        PacketRegistry.getInstance().register(DisconnectPeerPacket.class, new DisconnectPeerPacketHandler());
        PacketRegistry.getInstance().register(GroupInvitePacket.class, new GroupInvitePacketHandler());
        PacketRegistry.getInstance().register(HandshakePacket.class, new HandshakePacketHandler());
        PacketRegistry.getInstance().register(LeaveGroupPacket.class, new LeaveGroupPacketHandler());
        PacketRegistry.getInstance().register(LoginPacket.class, new LoginPacketHandler());
        PacketRegistry.getInstance().register(PeerMessagePacket.class, new PeerMessagePacketHandler());
        PacketRegistry.getInstance().register(GroupMessagePacket.class, new GroupMessagePacketHandler());
        PacketRegistry.getInstance().register(RegisterPacket.class, new RegisterPacketHandler());

        PacketRegistry.getInstance().register(ServerSettingsPacket.class, null);
        PacketRegistry.getInstance().register(SuccessPacket.class, null);
        PacketRegistry.getInstance().register(FailPacket.class, null);
        PacketRegistry.getInstance().register(DisconnectPacket.class, null);
        PacketRegistry.getInstance().register(ChatsPacket.class, null);
    }

    public static void registerStates() {

    }

    public static void registerCommands() {
        CommandRegistry.getInstance().register("stop", StopCommand.class, new StopCommand.StopCommandHandler());
        CommandRegistry.getInstance().register("help", HelpCommand.class, new HelpCommand.HelpCommandHandler());
    }

}
