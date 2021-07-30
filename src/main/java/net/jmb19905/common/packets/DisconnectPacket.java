package net.jmb19905.common.packets;

import java.nio.charset.StandardCharsets;

/**
 * Sent from to the Server to a Client to tell him that his peer disconnected
 */
public class DisconnectPacket extends Packet{
    public DisconnectPacket() {
        super("disconnect");
    }

    @Override
    public void construct(byte[] data) {
    }

    @Override
    public byte[] deconstruct() {
        return getId().getBytes(StandardCharsets.UTF_8);
    }
}
