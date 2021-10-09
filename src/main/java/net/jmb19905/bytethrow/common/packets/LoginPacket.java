package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

/**
 * Sent to the server to tell him the client's name. Sent from the server to the peer to tell him the client's name.
 */
public class LoginPacket extends Packet {

    private static final String ID = "login";

    public String name;
    public String password = " ";
    public boolean confirmIdentity = false;

    public LoginPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) throws ArrayIndexOutOfBoundsException {
        name = data[1];
        password = data[2];
        confirmIdentity = Boolean.parseBoolean(data[3]);
    }

    @Override
    public byte[] deconstruct() {
        String dataString = ID + "|" + name + "|" + password + "|" + confirmIdentity + "%";
        return dataString.getBytes(StandardCharsets.UTF_8);
    }

}
