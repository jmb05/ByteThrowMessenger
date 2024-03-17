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

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.jmbnetty.client.Client;

public class PacketManager {

    public static void sendChangeUsername(String name, Client client) {
        ChangeUserDataPacket packet = new ChangeUserDataPacket();
        packet.type = "username";
        packet.value = name;
        client.send(packet);
    }

    public static void sendChangePassword(String password, Client client) {
        ChangeUserDataPacket packet = new ChangeUserDataPacket();
        packet.type = "password";
        packet.value = password;
        client.send(packet);
    }

    public static void confirmIdentity(User user, Client client) {
        LoginPacket loginPacket = new LoginPacket();
        loginPacket.user = user;
        loginPacket.confirmIdentity = true;

        client.send(loginPacket);
    }

}
