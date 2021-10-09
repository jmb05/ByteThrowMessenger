/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.packets.DisconnectPacket;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class DisconnectPacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext channelHandlerContext, Packet packet, TcpServerHandler tcpServerHandler) {

    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) {
        String peerName = ((DisconnectPacket) packet).name;
        Chat chat = StartClient.manager.getChat(peerName);
        if(chat != null){
            chat.setActive(false);
            StartClient.window.setPeerStatus(peerName, false);
            StartClient.window.append(peerName, StartClient.window.getBold());
            StartClient.window.append(" disconnected", null);
            StartClient.window.newLine();
        }else {
            Logger.warn("Received invalid DisconnectPacket");
        }
    }
}
