package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.client.ClientPacketHandler;
import net.jmb19905.common.packets.handlers.client.DisconnectPacketHandler;

import java.nio.charset.StandardCharsets;

/**
 * Sent from to the Server to a Client to tell him that his peer disconnected
 */
public class DisconnectPacket extends Packet{

    public String name;

    public DisconnectPacket() {
        super("disconnect");
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

    @Override
    public ClientPacketHandler<? extends Packet> getClientPacketHandler() {
        return new DisconnectPacketHandler(this);
    }
}
