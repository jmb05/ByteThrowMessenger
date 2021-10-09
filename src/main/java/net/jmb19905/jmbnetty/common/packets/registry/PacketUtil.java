package net.jmb19905.jmbnetty.common.packets.registry;

import net.jmb19905.util.Logger;

import java.nio.charset.StandardCharsets;

public class PacketUtil {

    public static Packet construct(byte[] data) throws IllegalStateException{
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        PacketType<? extends Packet> packetType = null;
        try {
            packetType = PacketRegistry.getInstance().getPacketType(parts[0]);
            Packet packet = packetType.newPacketInstance();
            if (packet != null) {
                packet.construct(parts);
            }
            return packet;
        }catch (NoSuchMethodException e){
            Logger.error(e);
            return new Packet.NullPacket(packetType);
        }catch (NullPointerException e){
            Logger.error(e, "Packet has unknown type header: " + parts[0]);
            return new Packet.NullPacket(packetType);
        }
    }
}
