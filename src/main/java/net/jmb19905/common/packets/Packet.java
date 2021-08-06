package net.jmb19905.common.packets;

import net.jmb19905.common.exception.IllegalPacketSignatureException;
import net.jmb19905.common.packets.handlers.PacketHandler;

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
     */
    public static Packet constructPacket(byte[] data) throws IllegalPacketSignatureException {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        Packet packet;
        switch (parts[0]) {
            case "disconnect" -> packet = new DisconnectPacket();
            case "key_exchange" -> packet = new KeyExchangePacket();
            case "login" -> packet = new LoginPacket(false);
            case "register" -> packet = new LoginPacket(true);
            case "message" -> packet = new MessagePacket();
            case "fail" -> packet = new FailPacket();
            case "chats" -> packet = new ChatsPacket();
            case "connect" -> packet = new ConnectPacket();
            case "success" -> packet = new SuccessPacket();
            case "file_meta" -> packet = new FileMetaPacket();
            default -> throw new IllegalPacketSignatureException("Unexpected value: " + parts[0]);
        }
        if(packet != null) {
            packet.construct(data);
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

    public abstract <T extends Packet> PacketHandler<T> getPacketHandler();

    /**
     * @return the identifier that tells the receiver what the packet type is
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Packet{" + new String(deconstruct(), StandardCharsets.UTF_8) + '}';
    }
}
