package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.server.LoginPacketHandler;
import net.jmb19905.common.packets.handlers.server.ServerPacketHandler;

import java.nio.charset.StandardCharsets;

/**
 * Sent to the server to tell him the client's name. Sent from the server to the peer to tell him the client's name.
 */
public class LoginPacket extends Packet{

    public String name;
    public String password = " ";
    public boolean confirmIdentity = false;

    public LoginPacket(boolean register) {
        super(register ? "register" : "login");
    }

    @Override
    public void construct(byte[] data) throws ArrayIndexOutOfBoundsException {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        name = parts[1];
        password = parts[2];
        confirmIdentity = Boolean.parseBoolean(parts[3]);
    }

    @Override
    public byte[] deconstruct() {
        String dataString = getId() + "|" + name + "|" + password + "|" + confirmIdentity;
        return dataString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ServerPacketHandler<? extends Packet> getServerPacketHandler() {
        return new LoginPacketHandler(this);
    }
}
