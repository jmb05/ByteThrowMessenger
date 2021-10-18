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

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

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
    public void construct(String[] data) {
        switch (data[1]) {
            case "login" -> type = SuccessType.LOGIN;
            case "register" -> type = SuccessType.REGISTER;
            case "change_pw" -> type = SuccessType.CHANGE_PW;
            case "change_name" -> type = SuccessType.CHANGE_NAME;
            case "delete" -> type = SuccessType.DELETE;
        }
        confirmIdentity = Boolean.parseBoolean(data[2]);
    }

    @Override
    public byte[] deconstruct() {
        String dataString = ID + "|" + type.id + "|" + confirmIdentity;
        return dataString.getBytes(StandardCharsets.UTF_8);
    }

    public enum SuccessType{
        LOGIN("login"), REGISTER("register"), CHANGE_PW("change_pw"), CHANGE_NAME("change_name"), DELETE("delete");

        private final String id;

        SuccessType(String id){
            this.id = id;
        }
    }

}
