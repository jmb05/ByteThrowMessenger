package net.jmb19905.common.packets;

import net.jmb19905.common.util.SerializationUtility;

import java.nio.charset.StandardCharsets;

public class ConnectPacket extends Packet{

    public String name;
    public byte[] key;
    public boolean firstConnect;

    public ConnectPacket() {
        super("connect");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        name = parts[1];
        key = SerializationUtility.decodeBinary(parts[2]);
        firstConnect = Boolean.parseBoolean(parts[3]);
    }

    @Override
    public byte[] deconstruct() {
        String encodedKey = SerializationUtility.encodeBinary(key);
        return (getId() + "|" + name + "|" + encodedKey + "|" + firstConnect).getBytes(StandardCharsets.UTF_8);
    }
}
