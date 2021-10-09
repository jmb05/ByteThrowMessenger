package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

/**
 * Sent from to the Server to a Client to tell him that his peer disconnected
 */
public class DisconnectPacket extends Packet {

    private static final String ID = "disconnect";

    public String name;

    public DisconnectPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        name = data[1];
    }

    @Override
    public byte[] deconstruct() {
        return (ID + "|" + name + "%").getBytes(StandardCharsets.UTF_8);
    }

}
