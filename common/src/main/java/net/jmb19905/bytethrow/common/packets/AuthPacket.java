package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.buffer.SimpleBuffer;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

public class AuthPacket extends Packet {

    private final static String ID = "auth";

    public byte[] authSequence;

    protected AuthPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(SimpleBuffer buffer) {
        authSequence = buffer.getBytes();
    }

    @Override
    public void deconstruct(SimpleBuffer buffer) {
        buffer.putBytes(authSequence);
    }
}
