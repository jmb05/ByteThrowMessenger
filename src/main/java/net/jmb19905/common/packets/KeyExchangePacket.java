package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.client.ClientKeyExchangePacketHandler;
import net.jmb19905.common.packets.handlers.client.ClientPacketHandler;
import net.jmb19905.common.packets.handlers.server.ServerKeyExchangePacketHandler;
import net.jmb19905.common.packets.handlers.server.ServerPacketHandler;
import net.jmb19905.common.util.SerializationUtility;

import java.nio.charset.StandardCharsets;

/**
 * Transfers Public-Keys over the network
 */
public class KeyExchangePacket extends Packet{

    public String version = "null";
    public byte[] key;

    public KeyExchangePacket() {
        super("key_exchange");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        version = parts[1];
        key = SerializationUtility.decodeBinary(parts[2]);
    }

    @Override
    public byte[] deconstruct() {
        String encodedKey = SerializationUtility.encodeBinary(key);
        String data = getId() + "|" + version + "|" + encodedKey;
        return data.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ClientPacketHandler<? extends Packet> getClientPacketHandler() {
        return new ClientKeyExchangePacketHandler(this);
    }

    @Override
    public ServerPacketHandler<? extends Packet> getServerPacketHandler() {
        return new ServerKeyExchangePacketHandler(this);
    }
}
