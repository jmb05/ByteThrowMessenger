package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.KeyExchangePacketHandler;
import net.jmb19905.common.util.SerializationUtility;

import java.nio.charset.StandardCharsets;

/**
 * Transfers Public-Keys over the network
 */
public class KeyExchangePacket extends Packet{

    public byte[] key;

    public KeyExchangePacket() {
        super("key_exchange");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        key = SerializationUtility.decodeBinary(parts[1]);
    }

    @Override
    public byte[] deconstruct() {
        String encodedKey = SerializationUtility.encodeBinary(key);
        String data = getId() + "|" + encodedKey;
        return data.getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    @Override
    public KeyExchangePacketHandler getPacketHandler() {
        return new KeyExchangePacketHandler();
    }
}
