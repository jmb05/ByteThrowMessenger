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

/**
 * Sent to the client when an action is successful
 */
public class SuccessPacket extends Packet {

    private static final String ID = "success";

    public SuccessType type;
    public boolean confirmIdentity = false;

    public SuccessPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(SimpleBuffer buffer) {
        type = SuccessType.construct(buffer);
        confirmIdentity = buffer.getBoolean();
    }

    @Override
    public void deconstruct(SimpleBuffer buffer) {
        type.deconstruct(buffer);
        buffer.putBoolean(confirmIdentity);
    }

    public enum SuccessType {
        LOGIN("login"), REGISTER("register"), CHANGE_PW("change_pw"), CHANGE_NAME("change_name"), DELETE("delete");

        private final String id;

        SuccessType(String id){
            this.id = id;
        }

        public static SuccessType construct(SimpleBuffer buffer) {
            var s = buffer.getString();
            return switch (s) {
                case "register" -> REGISTER;
                case "change_pw" -> CHANGE_PW;
                case "change_name" -> CHANGE_NAME;
                case "delete" -> DELETE;
                default -> LOGIN;
            };
        }

        public void deconstruct(SimpleBuffer buffer) {
            buffer.putString(this.id);
        }
    }

}
