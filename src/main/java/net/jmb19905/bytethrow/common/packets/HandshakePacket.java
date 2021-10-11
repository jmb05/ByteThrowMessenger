package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.bytethrow.common.util.SerializationUtility;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

/**
 * Transfers Public-Keys over the network
 */
public class HandshakePacket extends Packet {

    private static final String ID = "handshake";

    public String version = "null";
    public byte[] key;

    public HandshakePacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        version = data[1];
        key = SerializationUtility.decodeBinary(data[2]);
    }

    @Override
    public byte[] deconstruct() {
        String data = ID + "|" + version + "|" + SerializationUtility.encodeBinary(key);
        return data.getBytes(StandardCharsets.UTF_8);
    }

}
