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

package net.jmb19905.bytethrow.client.packets;

import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.packets.ChatsRequestPacket;
import net.jmb19905.bytethrow.common.packets.SuccessPacket;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import javax.swing.*;

public class SuccessPacketHandler implements PacketHandler<SuccessPacket> {

    @Override
    public void handle(HandlingContext ctx, SuccessPacket packet) {
        switch (packet.type) {
            case LOGIN, REGISTER -> {
                doOnLoginSuccess(packet);
                StartClient.guiManager.showLoading(false);
            }
            case CHANGE_NAME -> JOptionPane.showMessageDialog(null, "Username changed successfully");
            case CHANGE_PW -> JOptionPane.showMessageDialog(null, "Password changed successfully");
            case DELETE -> {
                JOptionPane.showMessageDialog(null, "Deleted Account successfully");
                ShutdownManager.shutdown(0);
            }
        }
    }

    private void doOnLoginSuccess(SuccessPacket packet) {
        ClientManager manager = StartClient.manager;
        if (!packet.confirmIdentity) {
            if (!manager.loggedIn) {
                manager.loggedIn = true;
                ChatsRequestPacket chatsRequestPacket = new ChatsRequestPacket();
                manager.send(chatsRequestPacket);
            } else {
                Logger.warn("Already logged in");
            }
        } else {
            manager.confirmIdentityPacket = packet;
        }
        manager.confirmIdentity();
    }
}
