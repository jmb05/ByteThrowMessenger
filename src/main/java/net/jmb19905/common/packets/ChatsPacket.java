package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.client.ChatsPacketHandler;

import java.nio.charset.StandardCharsets;

public class ChatsPacket extends Packet{

    public String[] names;

    /**
     * Contains all the names of the peers of a client
     */
    public ChatsPacket() {
        super("chats");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        names = new String[parts.length - 1];
        System.arraycopy(parts, 1, names, 0, parts.length - 1);
    }

    @Override
    public byte[] deconstruct() {
        StringBuilder namesBuilder = new StringBuilder();
        for (String name : names) {
            if(name != null) {
                namesBuilder.append("|").append(name);
            }
        }
        return (getId() + namesBuilder).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ChatsPacketHandler getClientPacketHandler() {
        return new ChatsPacketHandler(this);
    }
}
