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

import net.jmb19905.net.buffer.BufferWrapper;
import net.jmb19905.net.packet.Packet;

public class FailPacket extends Packet {

    private static final String ID = "fail";

    public String cause;
    public String message;
    public String extra;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void construct(BufferWrapper buffer) {
        cause = buffer.getString();
        message = buffer.getString();
        extra = buffer.getString();
    }

    @Override
    public void deconstruct(BufferWrapper buffer) {
        buffer.putString(cause);
        buffer.putString(message);
        buffer.putString(extra);
    }
}
