package net.jmb19905.common.packets;

import net.jmb19905.common.exception.InvalidUsernameException;
import net.jmb19905.common.util.Logger;

import java.nio.charset.StandardCharsets;

/**
 * Data that is sent over the network
 */
public abstract class Packet {

    private final String id;

    public Packet(String id){
        this.id = id;
    }

    public static Packet constructPacket(byte[] data) throws InvalidUsernameException {
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
                try {
                    packet.construct(data);
                }catch (ArrayIndexOutOfBoundsException e){
                    Logger.log("Error constructing packet: " + new String(packet.deconstruct(), StandardCharsets.UTF_8), Logger.Level.ERROR);
                    throw new InvalidUsernameException("Invalid Username");
                }
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
