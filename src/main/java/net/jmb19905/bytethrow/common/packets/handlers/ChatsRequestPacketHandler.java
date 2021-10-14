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

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.common.packets.ChatsPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class ChatsRequestPacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler serverHandler) {
        ServerManager manager = StartServer.manager;
        String clientName = manager.getClientName(serverHandler);
        ChatsPacket chatsPacket = new ChatsPacket();
        chatsPacket.names = manager.getPeerNames(clientName);

        Logger.trace("Sending packet " + chatsPacket + " to " + ctx.channel().remoteAddress());
        NetworkingUtility.sendPacket(chatsPacket, ctx.channel(), serverHandler.getEncryption());
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) throws IllegalSideException {
        throw new IllegalSideException("ChatsRequestPacket received on Client");
    }
}