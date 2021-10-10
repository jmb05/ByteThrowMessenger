/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.packets.ChatsPacket;
import net.jmb19905.bytethrow.common.packets.ConnectPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class ChatsPacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext channelHandlerContext, Packet packet, TcpServerHandler handler) {
        //TODO: add IllegalSideException to lib
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler handler) {
        ChatsPacket chatsPacket = (ChatsPacket) packet;
        Logger.log(packet.toString(), Logger.Level.DEBUG);
        if(chatsPacket.update) {
            StartClient.manager.chats.clear();
        }
        for(String name : chatsPacket.names){
            Chat chat = new Chat();
            chat.initClient();
            chat.addClient(StartClient.manager.name);
            chat.addClient(name);

            StartClient.manager.chats.add(chat);

            if(!chatsPacket.update) {
                ConnectPacket connectPacket = new ConnectPacket();
                connectPacket.connectType = ConnectPacket.ConnectType.FIRST_RECONNECT;
                connectPacket.name = name;
                connectPacket.key = chat.encryption.getPublicKey().getEncoded();

                NetworkingUtility.sendPacket(connectPacket, channelHandlerContext.channel(), handler.getEncryption());
                Logger.log("Sent " + connectPacket, Logger.Level.TRACE);
            }
        }
        StartClient.guiManager.setPeers(chatsPacket.names);
    }
}
