/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common;

import net.jmb19905.bytethrow.common.packets.*;
import net.jmb19905.bytethrow.common.packets.handlers.*;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

public class RegistryManager {

    public static void registerAll(){
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
        PacketRegistry.getInstance().register("server_settings", ServerSettingsPacket.class, null);
        PacketRegistry.getInstance().register("success", SuccessPacket.class, new SuccessPacketHandler());
    }

}