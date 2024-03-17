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

package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.buffer.SimpleBuffer;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class FailPacket extends Packet {

    private static final String ID = "fail";

    public String cause;
    public String message;
    public String extra;

    /**
     * A Packet that is sent to a client to inform him that something went wrong
     */
    public FailPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(SimpleBuffer buffer) {
        cause = buffer.getString();
        message = buffer.getString();
        extra = buffer.getString();
    }

    @Override
    public void deconstruct(SimpleBuffer buffer) {
        buffer.putString(cause);
        buffer.putString(message);
        buffer.putString(extra);
    }
}
