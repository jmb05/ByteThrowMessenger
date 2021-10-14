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
import net.jmb19905.bytethrow.client.gui.Window;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.packets.DisconnectPacket;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class DisconnectPacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext channelHandlerContext, Packet packet, TcpServerHandler tcpServerHandler) throws IllegalSideException {
        throw new IllegalSideException("DisconnectPacket received on Server");
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) {
        String peerName = ((DisconnectPacket) packet).name;
        PeerChat chat = StartClient.manager.getChat(peerName);
        if(chat != null){
            chat.setActive(false);
            StartClient.guiManager.setPeerStatus(peerName, false);
            StartClient.guiManager.append(peerName, Window.getBold());
            StartClient.guiManager.append(" disconnected", null);
            StartClient.guiManager.newLine();
        }else {
            Logger.warn("Received invalid DisconnectPacket");
        }
    }
}
