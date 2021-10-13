/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.jmbnetty.common.packets.registry;

import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.util.Logger;
import net.jmb19905.util.registry.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PacketType<P extends Packet> extends Type<P> {

    private final Class<P> packetClass;
    private final PacketHandler handler;

    public PacketType(Class<P> packetClass, PacketHandler handler){
        this.packetClass = packetClass;
        this.handler = handler;
    }

    public PacketHandler handler() {
        return handler;
    }

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