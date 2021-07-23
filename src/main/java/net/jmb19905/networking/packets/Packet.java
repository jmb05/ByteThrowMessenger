package net.jmb19905.networking.packets;

import java.nio.charset.StandardCharsets;

/**
 * Data that is sent over the network
 */
public abstract class Packet {

    private final String id;

    public Packet(String id){
        this.id = id;
    }

    public static Packet constructPacket(byte[] data){
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        Packet packet;
        switch (parts[0]) {
            case "disconnect" -> {
                packet = new DisconnectPacket();
                packet.construct(data);
            }
            case "key_exchange" -> {
                packet = new KeyExchangePacket();
                packet.construct(data);
            }
            case "login" -> {
                packet = new LoginPacket();
                packet.construct(data);
            }
            case "message" -> {
                packet = new MessagePacket();
                packet.construct(data);
            }
            default -> throw new IllegalStateException("Unexpected value: " + parts[0]);
        }
        return packet;
    }

    public abstract Packet construct(byte[] data);

    public abstract byte[] deconstruct();

    public String getId() {
        return id;
    }
}
