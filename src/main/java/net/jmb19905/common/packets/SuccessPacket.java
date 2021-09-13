package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.client.ClientPacketHandler;
import net.jmb19905.common.packets.handlers.client.SuccessPacketHandler;

import java.nio.charset.StandardCharsets;

/**
 * Sent to the client when an action is successful
 */
public class SuccessPacket extends Packet{

    public String type;
    public boolean confirmIdentity = false;

    public SuccessPacket() {
        super("success");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        type = parts[1];
        confirmIdentity = Boolean.parseBoolean(parts[2]);
    }

    @Override
    public byte[] deconstruct() {
        String dataString = getId() + "|" + type + "|" + confirmIdentity;
        return dataString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ClientPacketHandler<? extends Packet> getClientPacketHandler() {
        return new SuccessPacketHandler(this);
    }
}
