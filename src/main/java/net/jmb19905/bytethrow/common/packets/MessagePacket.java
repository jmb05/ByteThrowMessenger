package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

/**
 * A Message from a client to his peer. Usually encrypted (E2EE).
 */
public class MessagePacket extends Packet {

    private static final String ID = "message";

    public Chat.Message message;

    public MessagePacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        message = new Chat.Message(data[1], data[2], data[3]);
    }

    @Override
    public byte[] deconstruct() {
        return (ID + "|" + message.sender() + "|" + message.receiver() + "|" + message.message()).getBytes(StandardCharsets.UTF_8);
    }
}