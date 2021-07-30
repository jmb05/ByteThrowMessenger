package net.jmb19905.common.packets;

import java.nio.charset.StandardCharsets;

public class DataPacket extends Packet{

    public String type;
    public String[] names;

    /**
     * Contains all the names of the peers of a client
     */
    public DataPacket() {
        super("data");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        type = parts[1];
        names = new String[parts.length - 2];
        System.arraycopy(parts, 2, names, 0, parts.length - 2);
    }

    @Override
    public byte[] deconstruct() {
        String firstPart = getId() + "|" + type;
        StringBuilder namesBuilder = new StringBuilder();
        for(String name : names){
            namesBuilder.append("|").append(name);
        }
        return (firstPart + namesBuilder).getBytes(StandardCharsets.UTF_8);
    }
}
