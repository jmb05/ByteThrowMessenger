package net.jmb19905.common.packets;

import java.nio.charset.StandardCharsets;

public class CreateChatPacket extends Packet{

    public String name;

    public CreateChatPacket() {
        super("create_chat");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        name = parts[1];
    }

    @Override
    public byte[] deconstruct() {
        return (getId() + "|" + name).getBytes(StandardCharsets.UTF_8);
    }
}
