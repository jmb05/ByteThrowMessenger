package net.jmb19905.networking.packets;

import java.nio.charset.StandardCharsets;

public class DisconnectPacket extends Packet{
    public DisconnectPacket() {
        super("disconnect");
    }

    @Override
    public DisconnectPacket construct(byte[] data) {
        return this;
    }

    @Override
    public byte[] deconstruct() {
        return getId().getBytes(StandardCharsets.UTF_8);
    }
}
