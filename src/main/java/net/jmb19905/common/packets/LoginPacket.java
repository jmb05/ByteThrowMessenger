package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.LoginPacketHandler;

import java.nio.charset.StandardCharsets;

/**
 * Sent to the server to tell him the client's name. Sent from the server to the peer to tell him the client's name.
 */
public class LoginPacket extends Packet{

    public String name;
    public String password = " ";

    public LoginPacket(boolean register) {
        super(register ? "register" : "login");
    }

    @Override
    public void construct(byte[] data) throws ArrayIndexOutOfBoundsException {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        name = parts[1];
        password = parts[2];
    }

    @Override
    public byte[] deconstruct() {
        String dataString = getId() + "|" + name + "|" + password;
        return dataString.getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    @Override
    public LoginPacketHandler getPacketHandler() {
        return new LoginPacketHandler();
    }
}
