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

import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

/**
 * A Message from a client to his peer. Usually encrypted (E2EE).
 */
public class MessagePacket extends Packet {

    private static final String ID = "message";

    public Chat.Message message;

    public MessagePacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        message = new Chat.Message(data[1], data[2], data[3], Boolean.parseBoolean(data[4]));
    }

    @Override
    public byte[] deconstruct() {
        return (ID + "|" + message.sender() + "|" + message.receiver() + "|" + message.message() + "|" + message.group()).getBytes(StandardCharsets.UTF_8);
    }
}