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

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.chat.PeerMessage;
import net.jmb19905.bytethrow.common.packets.PeerMessagePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;

public class PeerMessagePacketHandler implements PacketHandler<PeerMessagePacket> {

    @Override
    public void handle(HandlingContext ctx, PeerMessagePacket packet) {
        PeerMessage message = packet.message;
        ServerManager manager = StartServer.manager;
        User user = manager.getClient(ctx.getRemote());
        if (user.equals(message.getSender())) {
            if (user != null) {
                User peer = message.getReceiver();
                PeerChat chat = manager.getChat(user, peer);
                if (chat != null) {
                    if (chat.isActive()) {
                        manager.sendPacketToPeer(peer, packet, ctx.getRemote());
                        Logger.trace("Sent message to recipient: " + peer.getUsername());
                    } else {
                        NetworkingUtility.sendFail(ctx, "message", "peer_offline", peer.getUsername());
                    }
                } else {
                    NetworkingUtility.sendFail(ctx, "message", "no_such_chat", peer.getUsername());
                }
            } else {
                Logger.warn("Client is trying to communicate but isn't logged in!");
            }
        } else {
            Logger.warn("Received Message with wrong Sender!");
        }
    }
}
