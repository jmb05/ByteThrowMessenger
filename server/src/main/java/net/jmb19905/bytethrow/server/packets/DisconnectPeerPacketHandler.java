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

package net.jmb19905.bytethrow.server.packets;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.packets.DisconnectPeerPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;

public class DisconnectPeerPacketHandler extends PacketHandler<DisconnectPeerPacket> {
    @Override
    public void handle(ChannelHandlerContext ctx, DisconnectPeerPacket packet) throws IllegalSideException {
        ServerManager manager = StartServer.manager;

        User client = manager.getClient((TcpServerHandler) ctx.handler());
        User peer = packet.peer;


        PeerChat chat = manager.getChat(client, peer);
        manager.removeChat(chat);

        NetworkingUtility.sendPacket(packet, ctx);

        packet.peer = client;

        TcpServerHandler peerHandler = manager.getPeerHandler(peer, (TcpServerHandler) ctx.handler());
        if(peerHandler != null) {
            Channel channel = manager.getConnection().getClientConnections().get(peerHandler);
            NetworkingUtility.sendPacket(packet, channel, peerHandler.getEncryption());
        }
    }
}
