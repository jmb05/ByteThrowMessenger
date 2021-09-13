package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.server.ChatsRequestPacketHandler;

import java.nio.charset.StandardCharsets;

public class ChatsRequestPacket extends Packet{

    public ChatsRequestPacket() {
        super("chats_request");
    }

    @Override
    public void construct(byte[] data) {}

    @Override
    public byte[] deconstruct() {
        return getId().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ChatsRequestPacketHandler getServerPacketHandler() {
        return new ChatsRequestPacketHandler(this);
    }
}
