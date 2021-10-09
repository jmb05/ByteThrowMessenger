package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class FailPacket extends Packet {

    private static final String ID = "fail";

    public String cause;
    public String message;
    public String extra;

    /**
     * A Packet that is sent to a client to inform him that something went wrong
     */
    public FailPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        cause = data[1];
        message = data[2];
        extra = data[3];
    }

    @Override
    public byte[] deconstruct() {
        if(extra.equals("")){
            extra = " ";
        }
        return (ID + "|" + cause + "|" + message + "|" + extra + "%").getBytes(StandardCharsets.UTF_8);
    }
}
