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
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

import java.security.PublicKey;

public class HandshakePacketHandler extends PacketHandler<HandshakePacket> {

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, HandshakePacket packet) {
        Version packetVersion = new Version(packet.version);
        TcpServerHandler handler = (TcpServerHandler) ctx.handler();
        if (packetVersion.isInCompatible(StartServer.version)) {
            NetworkingUtility.sendFail(ctx, "version", "client_outdated", "");
            Logger.warn("Client tried to connect with outdated version: " + packet.version + " current version: " + StartServer.version);
            return;
        }

        byte[] clientEncodedPublicKey = packet.key;

        PublicKey clientPublicKey = EncryptionUtility.createPublicKeyFromData(clientEncodedPublicKey);
        handler.setPublicKey(clientPublicKey);

        Logger.info("Connection to Client: " + ctx.channel().remoteAddress() + " is encrypted");

        //change the key transferred in the packet to the server's PublicKey so the packet can be reused
        packet.key = handler.getEncryption().getPublicKey().getEncoded();

        Logger.trace("Sending packet " + packet + " to " + ctx.channel().remoteAddress());
        NetworkingUtility.sendPacket(packet, ctx.channel(), null);
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, HandshakePacket packet) {
        ClientManager manager = StartClient.manager;
        Version packetVersion = new Version(packet.version);
        if (packetVersion.isInCompatible(StartClient.version)) {
            StartClient.guiManager.showError("Client is outdated!");
            return;
        }
        ((TcpClientHandler) ctx.handler()).setPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
        manager.login(ctx);
    }
}
