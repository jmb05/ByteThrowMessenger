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
import net.jmb19905.bytethrow.common.Version;
import net.jmb19905.bytethrow.common.packets.HandshakePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

import java.security.PublicKey;

public class HandshakePacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext channelHandlerContext, Packet packet, TcpServerHandler tcpServerHandler) {
        HandshakePacket handshakePacket = (HandshakePacket) packet;
        Version packetVersion = new Version(handshakePacket.version);
        TcpServerConnection serverConnection = (TcpServerConnection) tcpServerHandler.getConnection();
        if (packetVersion.isInCompatible(StartServer.version)) {
            NetworkingUtility.sendFail(channelHandlerContext.channel(), "version", "client_outdated", "", tcpServerHandler);
            Logger.warn("Client tried to connect with outdated version: " + handshakePacket.version + " current version: " + StartServer.version);
            return;
        }

        byte[] clientEncodedPublicKey = handshakePacket.key;

        PublicKey clientPublicKey = EncryptionUtility.createPublicKeyFromData(clientEncodedPublicKey);
        tcpServerHandler.setPublicKey(clientPublicKey);

        Logger.info("Connection to Client: " + channelHandlerContext.channel().remoteAddress() + " is encrypted");

        //change the key transferred in the packet to the server's PublicKey so the packet can be reused
        handshakePacket.key = tcpServerHandler.getEncryption().getPublicKey().getEncoded();

        Logger.trace("Sending packet " + handshakePacket + " to " + channelHandlerContext.channel().remoteAddress());
        NetworkingUtility.sendPacket(handshakePacket, channelHandlerContext.channel(), null);
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) {
        ClientManager manager = StartClient.manager;
        HandshakePacket handshakePacket = (HandshakePacket) packet;
        Version packetVersion = new Version(handshakePacket.version);
        if (packetVersion.isInCompatible(StartClient.version)) {
            StartClient.guiManager.showError("Client is outdated!");
            return;
        }
        tcpClientHandler.setPublicKey(EncryptionUtility.createPublicKeyFromData(handshakePacket.key));
        manager.login(channelHandlerContext.channel(), tcpClientHandler.getEncryption());
    }
}
