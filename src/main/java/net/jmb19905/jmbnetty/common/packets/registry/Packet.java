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
