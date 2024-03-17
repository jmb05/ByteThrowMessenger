package net.jmb19905.bytethrow.client;

import net.jmb19905.bytethrow.client.packets.*;
import net.jmb19905.bytethrow.common.packets.*;
import net.jmb19905.net.packet.PacketRegistry;

public class ClientRegistryManager {

    public static void registerPackets() {
        PacketRegistry.getInstance().register(AddGroupMemberPacket.class, new AddGroupMemberPacketHandler());
        PacketRegistry.getInstance().register(ChatsPacket.class, new ChatsPacketHandler());
        PacketRegistry.getInstance().register(CreateGroupPacket.class, new CreateGroupPacketHandler());
        PacketRegistry.getInstance().register(ConnectPacket.class, new ConnectPacketHandler());
        PacketRegistry.getInstance().register(DisconnectPacket.class, new DisconnectPacketHandler());
        PacketRegistry.getInstance().register(DisconnectPeerPacket.class, new DisconnectPeerPacketHandler());
        PacketRegistry.getInstance().register(FailPacket.class, new FailPacketHandler());
        PacketRegistry.getInstance().register(GroupInvitePacket.class, new GroupInvitePacketHandler());
        PacketRegistry.getInstance().register(HandshakePacket.class, new HandshakePacketHandler());
        PacketRegistry.getInstance().register(LeaveGroupPacket.class, new LeaveGroupPacketHandler());
        PacketRegistry.getInstance().register(PeerMessagePacket.class, new PeerMessagePacketHandler());
        PacketRegistry.getInstance().register(GroupMessagePacket.class, new GroupMessagePacketHandler());
        PacketRegistry.getInstance().register(ServerSettingsPacket.class, new ServerSettingsPacketHandler());
        PacketRegistry.getInstance().register(SuccessPacket.class, new SuccessPacketHandler());

        PacketRegistry.getInstance().register(ChatsRequestPacket.class, null);
        PacketRegistry.getInstance().register(RegisterPacket.class, null);
        PacketRegistry.getInstance().register(LoginPacket.class, null);
        PacketRegistry.getInstance().register(ChangeUserDataPacket.class, null);
    }

    public static void registerStates() {
    }
}
