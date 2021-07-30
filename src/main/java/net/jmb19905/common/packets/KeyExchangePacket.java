package net.jmb19905.common.packets;

import net.jmb19905.common.util.SerializationUtility;

import java.nio.charset.StandardCharsets;

/**
 * Transfers Public-Keys over the network
 */
public class KeyExchangePacket extends Packet{

    /**
     * Tells the recipient if the key is meant for the Server or the Peer
     */
    public String recipient = "Server";
    public byte[] key;

    public KeyExchangePacket() {
        super("key_exchange");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        recipient = parts[1];
        key = SerializationUtility.decodeBinary(parts[2]);
    }

    @Override
    public byte[] deconstruct() {
        String encodedKey = SerializationUtility.encodeBinary(key);
        String data = getId() + "|" + recipient + "|" + encodedKey;
        return data.getBytes(StandardCharsets.UTF_8);
    }
}
