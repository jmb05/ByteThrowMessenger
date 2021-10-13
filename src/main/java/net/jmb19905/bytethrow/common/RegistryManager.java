/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.common;

import net.jmb19905.bytethrow.common.packets.*;
import net.jmb19905.bytethrow.common.packets.handlers.*;
import net.jmb19905.bytethrow.server.commands.HelpCommand;
import net.jmb19905.bytethrow.server.commands.StopCommand;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;
import net.jmb19905.util.commands.registry.CommandRegistry;

public class RegistryManager {

    public static void registerPackets(){
        PacketRegistry.getInstance().register("change_user_data", ChangeUserDataPacket.class, new ChangeUserDataPacketHandler());
        PacketRegistry.getInstance().register("chats", ChatsPacket.class, new ChatsPacketHandler());
        PacketRegistry.getInstance().register("chats_request", ChatsRequestPacket.class, new ChatsRequestPacketHandler());
        PacketRegistry.getInstance().register("connect", ConnectPacket.class, new ConnectPacketHandler());
        PacketRegistry.getInstance().register("disconnect", DisconnectPacket.class, new DisconnectPacketHandler());
        PacketRegistry.getInstance().register("fail", FailPacket.class, new FailPacketHandler());
        PacketRegistry.getInstance().register("handshake", HandshakePacket.class, new HandshakePacketHandler());
        PacketRegistry.getInstance().register("login", LoginPacket.class, new LoginPacketHandler());
        PacketRegistry.getInstance().register("message", MessagePacket.class, new MessagePacketHandler());
        PacketRegistry.getInstance().register("register", RegisterPacket.class, new RegisterPacketHandler());
        PacketRegistry.getInstance().register("server_settings", ServerSettingsPacket.class, new ServerSettingsPacketHandler());
        PacketRegistry.getInstance().register("success", SuccessPacket.class, new SuccessPacketHandler());
    }
}