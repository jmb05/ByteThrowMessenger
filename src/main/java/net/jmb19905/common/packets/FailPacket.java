package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.client.ClientPacketHandler;
import net.jmb19905.common.packets.handlers.client.FailPacketHandler;

import java.nio.charset.StandardCharsets;

public class FailPacket extends Packet{

    public String cause;
    public String message;
    public String extra;

    /**
     * A Packet that is sent to a client to inform him that something went wrong
     */
    public FailPacket() {
        super("fail");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        cause = parts[1];
        message = parts[2];
        extra = parts[3];
    }

    @Override
    public byte[] deconstruct() {
        if(extra.equals("")){
            extra = " ";
        }
        return (getId() + "|" + cause + "|" + message + "|" + extra).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ClientPacketHandler<? extends Packet> getClientPacketHandler() {
        return new FailPacketHandler(this);
    }
}
