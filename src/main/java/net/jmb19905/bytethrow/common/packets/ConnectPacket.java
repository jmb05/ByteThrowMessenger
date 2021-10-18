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

import net.jmb19905.bytethrow.common.util.SerializationUtility;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class ConnectPacket extends Packet {

    private static final String ID = "connect";

    public String name;
    public byte[] key;
    public ConnectType connectType;

    public ConnectPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] parts) {
        name = parts[1];
        key = SerializationUtility.decodeBinary(parts[2]);
        int typeInt = Integer.parseInt(parts[3]);
        if (typeInt == 0) {
            connectType = ConnectType.FIRST_CONNECT;
        } else if (typeInt == 1) {
            connectType = ConnectType.REPLY_CONNECT;
        } else if (typeInt == 2) {
            connectType = ConnectType.FIRST_RECONNECT;
        } else if (typeInt == 3) {
            connectType = ConnectType.REPLY_RECONNECT;
        }
    }

    @Override
    public byte[] deconstruct() {
        String encodedKey = SerializationUtility.encodeBinary(key);
        return (ID + "|" + name + "|" + encodedKey + "|" + connectType.typeInt).getBytes(StandardCharsets.UTF_8);
    }

    public enum ConnectType {
        FIRST_CONNECT(0), REPLY_CONNECT(1), FIRST_RECONNECT(2), REPLY_RECONNECT(3);

        private final int typeInt;

        ConnectType(int typeInt) {
            this.typeInt = typeInt;
        }
    }

}
