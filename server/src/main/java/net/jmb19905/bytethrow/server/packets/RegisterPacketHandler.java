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

package net.jmb19905.bytethrow.server.packets;

import net.jmb19905.bytethrow.common.packets.RegisterPacket;
import net.jmb19905.bytethrow.common.packets.SuccessPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;

import java.net.SocketAddress;

public class RegisterPacketHandler implements PacketHandler<RegisterPacket> {
    @Override
    public void handle(HandlingContext ctx, RegisterPacket packet) {
        Logger.trace("Client is trying to registering");

        if (DatabaseManager.createUser(packet.user)) {
            packet.user.removePassword();
            handleSuccessfulRegister(ctx, packet);
        } else {
            NetworkingUtility.sendFail(ctx, "register", "register_fail", "");
        }
    }

    /**
     * Things to do when a client logs in: -> set the client name -> create client file if it doesn't exist yet ->
     * tell the Client that the login succeeded -> tell the client which conversations he has started
     *
     * @param packet the login packet containing the login packet of the client
     */
    private void handleSuccessfulRegister(HandlingContext ctx, RegisterPacket packet) {
        ServerManager manager = StartServer.manager;
        if (manager.isClientOnline(packet.user)) {
            for (SocketAddress otherAddress : manager.getNetThread().getConnectedClients().keySet()) {
                if (manager.getClient(otherAddress).equals(packet.user)) {
                    NetworkingUtility.sendFail(manager.getNetThread(), otherAddress, "external_disconnect", "external_disconnect", "");
                    //future.addListener(future1 -> otherHandler.markClosed());
                }
            }
        }
        SocketAddress address = ctx.getRemote();
        manager.addOnlineClient(packet.user, address);
        Logger.info("Client: " + address + " now uses name: " + manager.getClient(address));

        sendRegisterSuccess(ctx); // confirms the register to the current client
    }

    /**
     * Sends LoginPacket to client to confirm login
     */
    private void sendRegisterSuccess(HandlingContext ctx) {
        SuccessPacket loginSuccessPacket = new SuccessPacket();
        loginSuccessPacket.type = SuccessPacket.SuccessType.REGISTER;

        Logger.trace("Sending packet " + loginSuccessPacket + " to " + ctx.getRemote());
        ctx.send(loginSuccessPacket);
    }
}
