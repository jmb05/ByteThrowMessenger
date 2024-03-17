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

package net.jmb19905.bytethrow.client.packets;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.packets.HandshakePacket;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.util.bootstrapping.Version;
import net.jmb19905.util.crypto.EncryptionUtility;

public class HandshakePacketHandler extends PacketHandler<HandshakePacket> {

    @Override
    public void handle(ChannelHandlerContext ctx, HandshakePacket packet) {
        ClientManager manager = StartClient.manager;
        Version packetVersion = new Version(packet.version);
        if (packetVersion.isIncompatible(StartClient.version)) {
            StartClient.guiManager.showError("Client is outdated!");
            return;
        }
        ((TcpClientHandler) ctx.handler()).setPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
        manager.login(ctx);
    }
}
