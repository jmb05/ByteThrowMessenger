package net.jmb19905.common.packets;

import net.jmb19905.common.Chat;

import java.nio.charset.StandardCharsets;

/**
 * A Message from a client to his peer. Usually encrypted (E2EE).
 */
public class MessagePacket extends Packet{

    public Chat.Message message;

    public MessagePacket() {
        super("message");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        message = new Chat.Message(parts[1], parts[2], parts[3]);
    }

    @Override
    public byte[] deconstruct() {
        return (getId() + "|" + message.sender() + "|" + message.receiver() + "|" + message.message()).getBytes(StandardCharsets.UTF_8);
    }
}
