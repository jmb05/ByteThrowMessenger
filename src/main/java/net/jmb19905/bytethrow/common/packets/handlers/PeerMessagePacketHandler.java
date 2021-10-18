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
import net.jmb19905.bytethrow.client.chat.ChatHistorySerialisation;
import net.jmb19905.bytethrow.client.chat.ClientPeerChat;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.chat.PeerMessage;
import net.jmb19905.bytethrow.common.packets.PeerMessagePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class PeerMessagePacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler serverHandler) {
        PeerMessagePacket messagePacket = (PeerMessagePacket) packet;
        PeerMessage message = messagePacket.message;
        ServerManager manager = StartServer.manager;
        String name = manager.getClientName(serverHandler);
        if (name.equals(message.getSender())) {
            if (!name.isBlank()) {
                String peerName = message.getReceiver();
                PeerChat chat = manager.getChat(name, peerName);
                if (chat != null) {
                    if (chat.isActive()) {
                        manager.sendPacketToPeer(peerName, messagePacket, serverHandler);
                        Logger.trace("Sent message to recipient: " + peerName);
                    } else {
                        NetworkingUtility.sendFail(ctx.channel(), "message", "peer_offline", peerName, serverHandler);
                    }
                } else {
                    NetworkingUtility.sendFail(ctx.channel(), "message", "no_such_chat", peerName, serverHandler);
                }
            } else {
                Logger.warn("Client is trying to communicate but isn't logged in!");
            }
        } else {
            Logger.warn("Received Message with wrong Sender!");
        }
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) {
        PeerMessagePacket messagePacket = (PeerMessagePacket) packet;
        PeerMessage message = messagePacket.message;
        String sender = message.getSender();
        String receiver = message.getReceiver();
        String encryptedMessage = message.getMessage();
        if (receiver.equals(StartClient.manager.name)) {
            ClientPeerChat chat = StartClient.manager.getChat(sender);
            if (chat != null) {
                message.setMessage(chat.getEncryption().isUsable() ? EncryptionUtility.decryptString(chat.getEncryption(), encryptedMessage) : encryptedMessage);
                StartClient.guiManager.appendMessage(message, chat);
                chat.addMessage(message);
                ChatHistorySerialisation.saveChat(StartClient.manager.name, chat);
            } else {
                Logger.warn("Received Packet from unknown user");
            }
        } else {
            Logger.warn("Received Packet destined for someone else");
        }
    }
}
