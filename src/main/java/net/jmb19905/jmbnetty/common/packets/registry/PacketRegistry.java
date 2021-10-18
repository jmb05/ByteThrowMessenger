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
import net.jmb19905.util.registry.Registry;

public class PacketRegistry extends Registry {

    private static final PacketRegistry instance = new PacketRegistry();

    public <P extends Packet> void register(String id, Class<P> packetClass, PacketHandler handler) {
        register(id, new PacketType<>(packetClass, handler));
    }

    public PacketType<? extends Packet> getPacketType(String id) {
        try {
            return (PacketType<? extends Packet>) getRegistry(id);
        } catch (NullPointerException e) {
            Logger.error("No such PacketType registered");
            return null;
        }
    }

    public static PacketRegistry getInstance() {
        return instance;
    }
}
