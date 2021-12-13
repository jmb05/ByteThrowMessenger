package net.jmb19905.demo.util;

import net.jmb19905.demo.packet.KeyExchangePacket;
import net.jmb19905.demo.packet.MessagePacket;
import net.jmb19905.demo.packet.handlers.KeyExchangePacketHandler;
import net.jmb19905.demo.packet.handlers.MessagePacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

public class RegistryManager {

    public static void registerPackets() {
        PacketRegistry.getInstance().register("key_exchange", KeyExchangePacket.class, new KeyExchangePacketHandler());
        PacketRegistry.getInstance().register("message", MessagePacket.class, new MessagePacketHandler());
    }

}
