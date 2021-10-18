/*
 * A simple Messenger written in Java
 * Copyright (C) 2020-2021  Jared M. Bennett
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.chat.ClientPeerChat;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.packets.DisconnectPeerPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class DisconnectPeerPacketHandler extends PacketHandler {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler handler) throws IllegalSideException {
        DisconnectPeerPacket disconnectPeerPacket = (DisconnectPeerPacket) packet;
        ServerManager manager = StartServer.manager;

        String clientName = manager.getClientName(handler);
        String peerName = disconnectPeerPacket.peer;


        PeerChat chat = manager.getChat(clientName, peerName);
        manager.removeChat(chat);

        NetworkingUtility.sendPacket(disconnectPeerPacket, ctx.channel(), handler.getEncryption());

        disconnectPeerPacket.peer = clientName;

        TcpServerHandler peerHandler = manager.getPeerHandler(peerName, handler);
        Channel channel = ((TcpServerConnection) peerHandler.getConnection()).getClientConnections().get(peerHandler);
        NetworkingUtility.sendPacket(disconnectPeerPacket, channel, peerHandler.getEncryption());
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet, TcpClientHandler handler) throws IllegalSideException {
        DisconnectPeerPacket disconnectPeerPacket = (DisconnectPeerPacket) packet;
        ClientManager manager = StartClient.manager;

        String peerName = disconnectPeerPacket.peer;

        ClientPeerChat chat = manager.getChat(peerName);
        manager.removeChat(chat);
        StartClient.guiManager.removeChat(chat);
        Logger.info("Disconnected from: " + peerName);
    }
}
