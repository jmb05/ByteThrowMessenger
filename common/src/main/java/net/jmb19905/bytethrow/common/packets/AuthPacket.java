package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.net.buffer.BufferWrapper;
import net.jmb19905.net.packet.Packet;

public class AuthPacket extends Packet {

    private final static String ID = "auth";

    public byte[] authSequence;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void deconstruct(BufferWrapper bufferWrapper) {
        authSequence = bufferWrapper.getBytes();
    }

    @Override
    public void construct(BufferWrapper bufferWrapper) {
        bufferWrapper.putBytes(authSequence);
    }
}
