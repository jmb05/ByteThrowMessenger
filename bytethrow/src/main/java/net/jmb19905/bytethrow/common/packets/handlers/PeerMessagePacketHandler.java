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
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.chat.PeerMessage;
import net.jmb19905.bytethrow.common.chat.client.ChatHistorySerialisation;
import net.jmb19905.bytethrow.common.chat.client.ClientPeerChat;
import net.jmb19905.bytethrow.common.packets.PeerMessagePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class PeerMessagePacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet) {
        PeerMessagePacket messagePacket = (PeerMessagePacket) packet;
        PeerMessage message = messagePacket.message;
        ServerManager manager = StartServer.manager;
        User user = manager.getClient((TcpServerHandler) ctx.handler());
        if (user.equals(message.getSender())) {
            if (user != null) {
                User peer = message.getReceiver();
                PeerChat chat = manager.getChat(user, peer);
                if (chat != null) {
                    if (chat.isActive()) {
                        manager.sendPacketToPeer(peer, messagePacket, manager.getConnection(), (TcpServerHandler) ctx.handler());
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

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet) {
        PeerMessagePacket messagePacket = (PeerMessagePacket) packet;
        PeerMessage message = messagePacket.message;
        User sender = message.getSender();
        User receiver = message.getReceiver();
        String encryptedMessage = message.getMessage();
        if (receiver.equals(StartClient.manager.user)) {
            ClientPeerChat chat = StartClient.manager.getChat(sender);
            if (chat != null) {
                message.setMessage(chat.getEncryption().isUsable() ? EncryptionUtility.decryptString(chat.getEncryption(), encryptedMessage) : encryptedMessage);
                StartClient.guiManager.appendMessage(message, chat);
                chat.addMessage(message);
                ChatHistorySerialisation.saveChat(StartClient.manager.user, chat);
            } else {
                Logger.warn("Received Packet from unknown user");
            }
        } else {
            Logger.warn("Received Packet destined for someone else");
        }
    }
}
