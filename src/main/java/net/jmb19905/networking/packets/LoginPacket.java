package net.jmb19905.networking.packets;

import java.nio.charset.StandardCharsets;

/**
 * Sent to the server to tell him the client's name. Sent from the server to the peer to tell him the client's name.
 */
public class LoginPacket extends Packet{

    public String name;

    public LoginPacket() {
        super("login");
    }

    @Override
    public LoginPacket construct(byte[] data) throws ArrayIndexOutOfBoundsException {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        name = parts[1];
        return this;
    }

    @Override
    public byte[] deconstruct() {
        return (getId() + "|" + name).getBytes(StandardCharsets.UTF_8);
    }

}
