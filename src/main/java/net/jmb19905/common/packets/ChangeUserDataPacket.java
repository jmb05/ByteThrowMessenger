package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.server.ChangeUserDataPacketHandler;
import net.jmb19905.common.packets.handlers.server.ServerPacketHandler;

import java.nio.charset.StandardCharsets;

public class ChangeUserDataPacket extends Packet{

    public String type = "";
    public String value = "";

    public ChangeUserDataPacket() {
        super("change_user_data");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        type = parts[1];
        value = parts[2];
    }

    @Override
    public byte[] deconstruct() {
        String dataString = getId() + "|" + type + "|" + value;
        return dataString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ServerPacketHandler<? extends Packet> getServerPacketHandler() {
        return new ChangeUserDataPacketHandler(this);
    }
}
