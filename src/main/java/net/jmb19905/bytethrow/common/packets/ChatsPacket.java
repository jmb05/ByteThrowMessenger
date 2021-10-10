package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class ChatsPacket extends Packet {

    private static final String ID = "chats";

    public String[] names;
    public boolean update = false;

    /**
     * Contains all the names of the peers of a client
     */
    public ChatsPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        names = new String[data.length - 2];
        System.arraycopy(data, 1, names, 0, data.length - 2);
        update = Boolean.parseBoolean(data[data.length - 1]);
    }

    @Override
    public byte[] deconstruct() {
        StringBuilder namesBuilder = new StringBuilder();
        for (String name : names) {
            if(name != null) {
                namesBuilder.append("|").append(name);
            }
        }
        return (ID + namesBuilder + "|" + update).getBytes(StandardCharsets.UTF_8);
    }

}
