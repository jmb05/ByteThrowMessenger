package net.jmb19905.bytethrow.client;

import net.jmb19905.bytethrow.client.packets.*;
import net.jmb19905.bytethrow.common.packets.*;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;
import net.jmb19905.jmbnetty.common.state.StateUpdateSubscribePacket;
import net.jmb19905.jmbnetty.common.state.StateUpdateSubscribePacketHandler;

public class ClientRegistryManager {

    public static void registerPackets() {PacketRegistry.getInstance().register("add_group_member", AddGroupMemberPacket.class, new AddGroupMemberPacketHandler());
        PacketRegistry.getInstance().register("chats", ChatsPacket.class, new ChatsPacketHandler());
        PacketRegistry.getInstance().register("create_group", CreateGroupPacket.class, new CreateGroupPacketHandler());
        PacketRegistry.getInstance().register("connect", ConnectPacket.class, new ConnectPacketHandler());
        PacketRegistry.getInstance().register("disconnect", DisconnectPacket.class, new DisconnectPacketHandler());
        PacketRegistry.getInstance().register("disconnect_peer", DisconnectPeerPacket.class, new DisconnectPeerPacketHandler());
        PacketRegistry.getInstance().register("fail", FailPacket.class, new FailPacketHandler());
        PacketRegistry.getInstance().register("group_invite", GroupInvitePacket.class, new GroupInvitePacketHandler());
        PacketRegistry.getInstance().register("handshake", HandshakePacket.class, new HandshakePacketHandler());
        PacketRegistry.getInstance().register("leave_group", LeaveGroupPacket.class, new LeaveGroupPacketHandler());
        PacketRegistry.getInstance().register("peer_message", PeerMessagePacket.class, new PeerMessagePacketHandler());
        PacketRegistry.getInstance().register("group_message", GroupMessagePacket.class, new GroupMessagePacketHandler());
        PacketRegistry.getInstance().register("server_settings", ServerSettingsPacket.class, new ServerSettingsPacketHandler());
        PacketRegistry.getInstance().register("success", SuccessPacket.class, new SuccessPacketHandler());
        PacketRegistry.getInstance().register("state_listener", StateUpdateSubscribePacket.class, new StateUpdateSubscribePacketHandler());

        PacketRegistry.getInstance().register("chats_request", ChatsRequestPacket.class, null);
        PacketRegistry.getInstance().register("register", RegisterPacket.class, null);
        PacketRegistry.getInstance().register("login", LoginPacket.class, null);
        PacketRegistry.getInstance().register("change_user_data", ChangeUserDataPacket.class, null);
    }

    public static void registerStates() {
    }
}
