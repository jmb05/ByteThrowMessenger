package net.jmb19905.demo.packet;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class MessagePacket extends Packet {

    private static final String ID = "message";

    public String message;

    public MessagePacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        message = data[1];
    }

    @Override
    public byte[] deconstruct() {
        return (ID + "|" + message).getBytes(StandardCharsets.UTF_8);
    }
}
