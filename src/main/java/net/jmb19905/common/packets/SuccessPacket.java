package net.jmb19905.common.packets;

import java.nio.charset.StandardCharsets;

public class SuccessPacket extends Packet{

    public String type;

    public SuccessPacket() {
        super("success");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        type = parts[1];
    }

    @Override
    public byte[] deconstruct() {
        String dataString = getId() + "|" + type;
        return dataString.getBytes(StandardCharsets.UTF_8);
    }
}
