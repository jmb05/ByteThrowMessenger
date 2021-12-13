package net.jmb19905.demo.packet;

import net.jmb19905.demo.util.SerializationUtility;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class KeyExchangePacket extends Packet {

    private static final String ID = "key_exchange";

    public byte[] key;

    public KeyExchangePacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        key = SerializationUtility.decodeBinary(data[1]);
    }

    @Override
    public byte[] deconstruct() {
        return (ID + "|" + SerializationUtility.encodeBinary(key)).getBytes(StandardCharsets.UTF_8);
    }
}
