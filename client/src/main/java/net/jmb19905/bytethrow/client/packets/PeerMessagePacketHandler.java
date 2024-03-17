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

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.PeerMessage;
import net.jmb19905.bytethrow.common.chat.client.ChatHistorySerialisation;
import net.jmb19905.bytethrow.common.chat.client.ClientPeerChat;
import net.jmb19905.bytethrow.common.packets.PeerMessagePacket;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.util.Logger;
import net.jmb19905.util.crypto.EncryptionUtility;

public class PeerMessagePacketHandler extends PacketHandler<PeerMessagePacket> {
    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, PeerMessagePacket packet) {
        PeerMessage message = packet.message;
        User sender = message.getSender();
        User receiver = message.getReceiver();
        String encryptedMessage = message.getMessage();
        if (receiver.equals(StartClient.manager.user)) {
            ClientPeerChat chat = StartClient.manager.getChat(sender);
            if (chat != null) {
                message.setMessage(chat.getEncryption().isUsable() ? EncryptionUtility.decryptString(chat.getEncryption(), encryptedMessage) : encryptedMessage);
                StartClient.guiManager.appendMessage(message, chat, false);
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
