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

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.common.packets.RegisterPacket;
import net.jmb19905.bytethrow.common.packets.SuccessPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class RegisterPacketHandler extends PacketHandler<RegisterPacket> {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, RegisterPacket packet) {
        Logger.trace("Client is trying to registering");

        ServerManager manager = StartServer.manager;

        if (DatabaseManager.createUser(packet.user)) {
            packet.user.removePassword();
            handleSuccessfulRegister(ctx, packet, manager.getConnection());
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
    private void handleSuccessfulRegister(ChannelHandlerContext ctx, RegisterPacket packet, TcpServerConnection connection) {
        ServerManager manager = StartServer.manager;
        if (manager.isClientOnline(packet.user)) {
            for (TcpServerHandler otherHandler : connection.getClientConnections().keySet()) {
                if (manager.getClient(otherHandler).equals(packet.user)) {
                    SocketChannel otherSocketChannel = connection.getClientConnections().get(otherHandler);
                    NetworkingUtility.sendFail(otherSocketChannel, "external_disconnect", "external_disconnect", "", otherHandler.getEncryption());
                    //future.addListener(future1 -> otherHandler.markClosed());
                }
            }
        }
        TcpServerHandler handler = (TcpServerHandler) ctx.handler();
        manager.addOnlineClient(packet.user, handler);
        Logger.info("Client: " + ctx.channel().remoteAddress() + " now uses name: " + manager.getClient(handler));

        sendRegisterSuccess(ctx.channel(), handler); // confirms the register to the current client
    }

    /**
     * Sends LoginPacket to client to confirm login
     */
    private void sendRegisterSuccess(Channel channel, TcpServerHandler handler) {
        SuccessPacket loginSuccessPacket = new SuccessPacket();
        loginSuccessPacket.type = SuccessPacket.SuccessType.REGISTER;

        Logger.trace("Sending packet " + loginSuccessPacket + " to " + channel.remoteAddress());
        NetworkingUtility.sendPacket(loginSuccessPacket, channel, handler.getEncryption());
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, RegisterPacket packet) throws IllegalSideException {
        throw new IllegalSideException("RegisterPacket received on Client");
    }
}
