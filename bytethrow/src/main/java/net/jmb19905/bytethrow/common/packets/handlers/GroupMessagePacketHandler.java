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

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.chat.GroupMessage;
import net.jmb19905.bytethrow.common.chat.client.ChatHistorySerialisation;
import net.jmb19905.bytethrow.common.chat.client.ClientGroupChat;
import net.jmb19905.bytethrow.common.packets.GroupMessagePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class GroupMessagePacketHandler extends PacketHandler {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet) throws IllegalSideException {
        GroupMessagePacket messagePacket = (GroupMessagePacket) packet;
        GroupMessage message = messagePacket.message;
        ServerManager manager = StartServer.manager;
        TcpServerConnection connection = manager.getConnection();
        TcpServerHandler handler = (TcpServerHandler) ctx.handler();
        User user = manager.getClient(handler);
        if (user.equals(message.getSender())) {
            if (user != null) {
                String groupName = message.getGroupName();
                GroupChat chat = manager.getGroup(groupName);
                if (chat != null) {
                    manager.sendPacketToGroup(groupName, packet, connection, handler);
                    Logger.trace("Sent message to group: " + groupName);
                } else {
                    NetworkingUtility.sendFail(ctx, "message", "no_such_chat", groupName);
                }
            } else {
                Logger.warn("Client is trying to communicate but isn't logged in!");
            }
        } else {
            Logger.warn("Received Message with wrong Sender! (" + user.getUsername() + " != " + message.getSender() + ")");
        }
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet) throws IllegalSideException {
        GroupMessagePacket messagePacket = (GroupMessagePacket) packet;
        GroupMessage message = messagePacket.message;
        String groupName = message.getGroupName();
        ClientGroupChat chat = StartClient.manager.getGroup(groupName);
        if (chat != null) {
            StartClient.guiManager.appendMessage(message, chat);
            chat.addMessage(message);
            ChatHistorySerialisation.saveChat(StartClient.manager.user, chat);
        } else {
            Logger.warn("Received Message from invalid chat");
        }
    }
}
