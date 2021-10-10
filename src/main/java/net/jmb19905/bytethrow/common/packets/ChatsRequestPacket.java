package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class ChatsRequestPacket extends Packet {

    private static final String ID = "chats_request";

    public ChatsRequestPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] strings) {}

    @Override
    public byte[] deconstruct() {
        return ID.getBytes(StandardCharsets.UTF_8);
    }

}
