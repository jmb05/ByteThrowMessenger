/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.packets.ChatsRequestPacket;
import net.jmb19905.bytethrow.common.packets.SuccessPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import javax.swing.*;

public class SuccessPacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext channelHandlerContext, Packet packet, TcpServerHandler tcpServerHandler) throws IllegalSideException {
        throw new IllegalSideException("SuccessPacket received on Server");
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet, TcpClientHandler tcpClientHandler) {
        SuccessPacket successPacket = (SuccessPacket) packet;
        Encryption encryption = tcpClientHandler.getEncryption();
        switch (successPacket.type) {
            case "login", "register" -> {
                doOnLoginSuccess(successPacket, encryption, ctx.channel());
                StartClient.guiManager.showLoading(false);
            }
            case "change_username" -> JOptionPane.showMessageDialog(null, "Username changed successfully");
            case "change_password" -> JOptionPane.showMessageDialog(null, "Password changed successfully");
            case "delete" -> {
                JOptionPane.showMessageDialog(null, "Deleted Account successfully");
                ShutdownManager.shutdown(0);
            }
        }
    }

    private void doOnLoginSuccess(SuccessPacket packet, Encryption encryption, Channel channel){
        ClientManager manager = StartClient.manager;
        if(!packet.confirmIdentity) {
            if(!manager.loggedIn) {
                StartClient.guiManager.appendLine("Login successful");
                manager.loggedIn = true;
                ChatsRequestPacket chatsRequestPacket = new ChatsRequestPacket();
                NetworkingUtility.sendPacket(chatsRequestPacket, channel, encryption);
            }else {
                Logger.warn("Already logged in");
            }
        }else {
            manager.confirmIdentityPacket = packet;
        }
        manager.confirmIdentity();
    }
}
