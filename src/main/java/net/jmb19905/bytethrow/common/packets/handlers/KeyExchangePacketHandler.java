/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.networking.ClientManager;
import net.jmb19905.bytethrow.common.Version;
import net.jmb19905.bytethrow.common.packets.KeyExchangePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.util.Logger;

import javax.swing.*;
import java.security.PublicKey;

public class KeyExchangePacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext channelHandlerContext, Packet packet, TcpServerHandler tcpServerHandler) {
        KeyExchangePacket keyExchangePacket = (KeyExchangePacket) packet;
        Version packetVersion = new Version(keyExchangePacket.version);
        if(packetVersion.isInCompatible(StartServer.version)){
            NetworkingUtility.sendFail(channelHandlerContext.channel(), "version", "client_outdated", "", (TcpServerConnection) tcpServerHandler.getConnection());
            Logger.warn("Client tried to connect with outdated version: " + keyExchangePacket.version + " current version: " + StartServer.version);
            return;
        }

        byte[] clientEncodedPublicKey = keyExchangePacket.key;

        PublicKey clientPublicKey;
        clientPublicKey = EncryptionUtility.createPublicKeyFromData(clientEncodedPublicKey);
        tcpServerHandler.getConnection().setPublicKey(clientPublicKey);

        Logger.log("Connection to Client: " + channelHandlerContext.channel().remoteAddress() + " is encrypted", Logger.Level.INFO);

        //change the key transferred in the packet to the server's PublicKey so the packet can be reused
        keyExchangePacket.key = tcpServerHandler.getConnection().getEncryption().getPublicKey().getEncoded();

        Logger.log("Sending packet " + keyExchangePacket + " to " + channelHandlerContext.channel().remoteAddress() , Logger.Level.TRACE);
        NetworkingUtility.sendPacket(keyExchangePacket, channelHandlerContext.channel(), null);
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) {
        ClientManager manager = StartClient.manager;
        KeyExchangePacket keyExchangePacket = (KeyExchangePacket) packet;
        Version packetVersion = new Version(keyExchangePacket.version);
        if(packetVersion.isInCompatible(StartClient.version)){
            JOptionPane.showMessageDialog(StartClient.window,"Client is outdated!", "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tcpClientHandler.getConnection().setPublicKey(EncryptionUtility.createPublicKeyFromData(keyExchangePacket.key));
        StartClient.window.appendLine("Connection to Server encrypted");
        manager.login(channelHandlerContext.channel(), tcpClientHandler.getConnection().getEncryption());
    }
}
