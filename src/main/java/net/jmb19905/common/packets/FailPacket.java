package net.jmb19905.common.packets;

import java.nio.charset.StandardCharsets;

public class FailPacket extends Packet{

    public String cause;

    public FailPacket() {
        super("fail");
    }

    @Override
    public void construct(byte[] data) {
        cause = new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] deconstruct() {
        return cause.getBytes(StandardCharsets.UTF_8);
    }
}
