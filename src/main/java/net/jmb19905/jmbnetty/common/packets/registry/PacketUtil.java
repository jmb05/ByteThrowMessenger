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

import net.jmb19905.util.Logger;

import java.nio.charset.StandardCharsets;

public class PacketUtil {

    public static Packet construct(byte[] data) throws IllegalStateException {
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
        } catch (NoSuchMethodException e) {
            Logger.error(e);
            return new Packet.NullPacket(packetType);
        } catch (NullPointerException e) {
            Logger.error(e, "Packet has unknown type header: " + parts[0]);
            return new Packet.NullPacket(packetType);
        }
    }
}
