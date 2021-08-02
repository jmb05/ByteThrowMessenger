package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.FailPacketHandler;

import java.nio.charset.StandardCharsets;

public class FailPacket extends Packet{

    public String cause;
    public String message;

    /**
     * A Packet that is sent from the server to a client to inform him that something went wrong
     */
    public FailPacket() {
        super("fail");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        cause = parts[1];
        message = parts[2];
    }

    @Override
    public byte[] deconstruct() {
        return (getId() + "|" + cause + "|" + message).getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FailPacketHandler getPacketHandler() {
        return new FailPacketHandler();
    }
}
