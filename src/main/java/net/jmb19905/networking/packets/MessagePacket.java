package net.jmb19905.networking.packets;

import java.nio.charset.StandardCharsets;

public class MessagePacket extends Packet{

    public String message;

    public MessagePacket() {
        super("message");
    }

    @Override
    public Packet construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        message = parts[1];
        return this;
    }

    @Override
    public byte[] deconstruct() {
        return (getId() + "|" + message).getBytes(StandardCharsets.UTF_8);
    }
}
