package net.jmb19905.jmbnetty.common.packets.registry;

import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.util.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public record PacketType<P extends Packet>(Class<P> packetClass, PacketHandler handler) {

    public P newPacketInstance() throws NoSuchMethodException{
        try {
            Constructor<P> constructor = packetClass.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Logger.error(e);
            return null;
        }
    }

}
