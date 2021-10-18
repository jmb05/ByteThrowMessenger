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
import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.chat.ChatHistorySerialisation;
import net.jmb19905.bytethrow.client.chat.ClientGroupChat;
import net.jmb19905.bytethrow.client.chat.ClientPeerChat;
import net.jmb19905.bytethrow.common.packets.ChatsPacket;
import net.jmb19905.bytethrow.common.packets.ConnectPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class ChatsPacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext channelHandlerContext, Packet packet, TcpServerHandler handler) throws IllegalSideException {
        throw new IllegalSideException("Chats received on Server");
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler handler) {
        ChatsPacket chatsPacket = (ChatsPacket) packet;
        ClientManager manager = StartClient.manager;
        if (chatsPacket.update) {
            manager.clearChats();
        }
        for (ChatsPacket.ChatData chatData : chatsPacket.chatData) {
            if (chatData.name().equals("null")) {
                String peerName = chatData.members().stream().filter(s -> !s.equals(manager.name)).findFirst().orElse(null);

                ClientPeerChat chat = new ClientPeerChat(chatData);
                chat.merge(ChatHistorySerialisation.readChat(manager.name, chat.getUniqueId()));
                ChatHistorySerialisation.saveChat(manager.name, chat);

                manager.addChat(chat);

                chat.initClient();

                if (!chatsPacket.update) {
                    ConnectPacket connectPacket = new ConnectPacket();
                    connectPacket.connectType = ConnectPacket.ConnectType.FIRST_RECONNECT;
                    connectPacket.name = peerName;
                    connectPacket.key = chat.getEncryption().getPublicKey().getEncoded();

                    NetworkingUtility.sendPacket(connectPacket, channelHandlerContext.channel(), handler.getEncryption());
                    Logger.trace("Sent " + connectPacket);
                }
            } else {
                ClientGroupChat chat = new ClientGroupChat(chatData);
                chat.merge(ChatHistorySerialisation.readChat(manager.name, chat.getUniqueId()));
                ChatHistorySerialisation.saveChat(manager.name, chat);

                StartClient.manager.addGroup(chat);
            }
        }
    }
}
