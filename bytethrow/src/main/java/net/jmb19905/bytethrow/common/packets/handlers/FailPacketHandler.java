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
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.client.ClientGroupChat;
import net.jmb19905.bytethrow.common.chat.client.ClientPeerChat;
import net.jmb19905.bytethrow.common.packets.FailPacket;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

public class FailPacketHandler extends PacketHandler<FailPacket> {

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, FailPacket packet) throws IllegalSideException {
        throw new IllegalSideException("FailPacket received on Server");
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, FailPacket packet) {
        ClientManager manager = StartClient.manager;
        String cause = packet.cause;
        String message = Localisation.get(packet.message);
        if (!packet.extra.equals(" ")) {
            message = Localisation.get(packet.message, packet.extra);
        }
        StartClient.guiManager.showError(message);
        switch (cause.split(":")[0]) {
            case "login" -> manager.relogin(ctx);
            case "register" -> manager.register(ctx);
            case "version" -> {
                Logger.fatal("Version mismatch: " + Localisation.get(packet.message));
                ShutdownManager.shutdown(-1);
            }
            case "external_disconnect" -> ShutdownManager.shutdown(0);
            case "connect" -> {
                String peerName = cause.split(":")[1];
                User peer = new User(peerName);
                ClientPeerChat chat = manager.getChat(peer);
                manager.removeChat(chat);
            }
            case "group_add" -> {
                String[] parts = cause.split(":");
                String memberName = parts[1];
                User member = new User(memberName);
                String groupName = parts[2];
                ClientGroupChat chat = manager.getGroup(groupName);
                if(chat != null) {
                    chat.removeClient(member);
                    if (chat.getMembers().size() < 2) {
                        manager.removeGroup(chat);
                    }
                }
            }
        }
    }
}
