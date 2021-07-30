package net.jmb19905.common.packets;

import net.jmb19905.common.exception.InvalidLoginException;
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

    /**
     * constructs a Packet from a byte-array
     * @param data the raw Packet as a byte-array
     * @return the Packet
     * @throws InvalidLoginException if the Packet is a LoginPacket and the username is invalid
     */
    public static Packet constructPacket(byte[] data) throws InvalidLoginException {
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
                    throw new InvalidLoginException("Invalid Username");
                }
            }
            case "message" -> {
                packet = new MessagePacket();
                packet.construct(data);
            }
            case "fail" -> {
                packet = new FailPacket();
                packet.construct(data);
            }
            case "data" -> {
                packet = new DataPacket();
                packet.construct(data);
            }
            case "create_chat" -> {
                packet = new CreateChatPacket();
                packet.construct(data);
            }
            default -> throw new IllegalStateException("Unexpected value: " + parts[0]);
        }
        return packet;
    }

    /**
     * How to construct a Packet from a byte-array
     * @param data the byte-array
     */
    public abstract void construct(byte[] data);

    /**
     * @return a byte-array the contains all the data of the Packet
     */
    public abstract byte[] deconstruct();

    /**
     * @return the identifier that tells the receiver what the packet type is
     */
    public String getId() {
        return id;
    }
}
