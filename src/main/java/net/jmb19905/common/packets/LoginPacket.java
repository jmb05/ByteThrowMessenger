package net.jmb19905.common.packets;

import java.nio.charset.StandardCharsets;

/**
 * Sent to the server to tell him the client's name. Sent from the server to the peer to tell him the client's name.
 */
public class LoginPacket extends Packet{

    public boolean register = false;
    public String name;
    public String password;

    public LoginPacket() {
        super("login");
    }

    @Override
    public void construct(byte[] data) throws ArrayIndexOutOfBoundsException {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        register = Boolean.parseBoolean(parts[1]);
        name = parts[2];
        password = parts[3];
        System.out.println(parts[1]);
        System.out.println("Constructed LoginPacket:" + this);
    }

    @Override
    public byte[] deconstruct() {
        String dataString = getId() + "|" + register + "|" + name + "|" + password;
        return dataString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "LoginPacket{" +
                "register=" + register +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
