package net.jmb19905.jmbnetty.common.packets.registry;

import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;

import java.nio.charset.StandardCharsets;

public abstract class Packet {

    private final PacketType<? extends Packet> type;

    protected Packet(PacketType<? extends Packet> type) {
        this.type = type;
    }

    public abstract void construct(String[] data);

    public abstract byte[] deconstruct();

    @Override
    public String toString() {
        return new String(deconstruct(), StandardCharsets.UTF_8);
    }

    public PacketType<? extends Packet> getType() {
        return type;
    }

    public PacketHandler getHandler() {
        return type.handler();
    }

    public static class NullPacket extends Packet{

        protected NullPacket(PacketType<? extends Packet> type) {
            super(type);
        }

        @Override
        public void construct(String[] data) {}

        @Override
        public byte[] deconstruct() {
            return new byte[0];
        }
    }
}
