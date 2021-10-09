package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class ChangeUserDataPacket extends Packet {

    private static final String ID = "change_user_data";

    public String type = "";
    public String value = "";

    public ChangeUserDataPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }


    @Override
    public void construct(String[] strings) {
        type = strings[1];
        value = strings[2];
    }

    @Override
    public byte[] deconstruct() {
        String dataString = ID + "|" + type + "|" + value + "%";
        return dataString.getBytes(StandardCharsets.UTF_8);
    }

}
