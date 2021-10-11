package net.jmb19905.jmbnetty.common.packets.registry;

import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketRegistry {

    private static final PacketRegistry instance = new PacketRegistry();

    private final Map<String, PacketType<? extends Packet>> packetTypes;

    public PacketRegistry() {
        this.packetTypes = new ConcurrentHashMap<>();
    }

    public <P extends Packet> void register(String id, Class<P> packetClass, PacketHandler handler){
        packetTypes.put(id, new PacketType<>(packetClass, handler));
    }

    public PacketType<? extends Packet> getPacketType(String id) throws NullPointerException{
        PacketType<? extends Packet> packetType = packetTypes.get(id);
        if(packetType == null){
            throw new NullPointerException("No Such PacketType");
        }
        return packetType;
    }

    public static PacketRegistry getInstance() {
        return instance;
    }
}
