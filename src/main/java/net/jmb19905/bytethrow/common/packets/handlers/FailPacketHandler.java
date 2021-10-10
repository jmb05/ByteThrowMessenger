/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.packets.FailPacket;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

public class FailPacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler tcpServerHandler) throws IllegalSideException {
        throw new IllegalSideException("FailPacket received on Server");
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet, TcpClientHandler tcpClientHandler) {
        ClientManager manager = StartClient.manager;
        FailPacket failPacket = (FailPacket) packet;
        String cause = failPacket.cause;
        String message = Localisation.get(failPacket.message);
        Encryption encryption = tcpClientHandler.getEncryption();
        if(!failPacket.extra.equals(" ")){
            message = Localisation.get(failPacket.message, failPacket.extra);
        }
        StartClient.guiManager.showError(message);
        switch (cause.split(":")[0]) {
            case "login" -> manager.login(ctx.channel(), encryption);
            case "register" -> manager.register(ctx.channel(), encryption);
            case "version" -> {
                Logger.fatal("Version mismatch: " + failPacket.message);
                StartClient.guiManager.showError(failPacket.message, "Version mismatch");
                ShutdownManager.shutdown(-1);
            }
            case "external_disconnect" -> ShutdownManager.shutdown(0);
            case "connect" -> {
                String peerName = cause.split(":")[1];
                Chat chat = manager.getChat(peerName);
                manager.chats.remove(chat);
                StartClient.guiManager.removePeer(peerName);
            }
        }
    }
}