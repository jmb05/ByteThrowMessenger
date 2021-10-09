package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

/**
 * Sent to the client when an action is successful
 */
public class SuccessPacket extends Packet {

    private static final String ID = "success";

    public String type;
    public boolean confirmIdentity = false;

    public SuccessPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        type = data[1];
        confirmIdentity = Boolean.parseBoolean(data[2]);
    }

    @Override
    public byte[] deconstruct() {
        String dataString = ID + "|" + type + "|" + confirmIdentity + "%";
        return dataString.getBytes(StandardCharsets.UTF_8);
    }
}
