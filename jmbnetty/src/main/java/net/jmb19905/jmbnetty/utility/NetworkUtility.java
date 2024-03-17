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

package net.jmb19905.jmbnetty.utility;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import net.jmb19905.jmbnetty.common.buffer.SimpleBuffer;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;
import net.jmb19905.util.crypto.Encryption;

import java.net.InetSocketAddress;

public class NetworkUtility {

    public static ChannelFuture sendTcp(Channel channel, Packet packet, Encryption encryption) {
        SimpleBuffer encrypted = encryptPacket(packet, encryption, channel.alloc());
        return channel.writeAndFlush(encrypted.getBuffer());
    }

    public static ChannelFuture sendTcp(ChannelHandlerContext ctx, Packet packet, Encryption encryption) {
        SimpleBuffer encrypted = encryptPacket(packet, encryption, ctx.alloc());
        return ctx.writeAndFlush(encrypted.getBuffer());
    }

    public static ChannelFuture sendUdp(NioDatagramChannel channel, Packet packet, InetSocketAddress receiver, Encryption encryption) {
        return channel.writeAndFlush(writeToDatagram(encryptPacket(packet, encryption, channel.alloc()), receiver));
    }

    public static ChannelFuture sendUdp(ChannelHandlerContext ctx, Packet packet, InetSocketAddress receiver, Encryption encryption) {
        return ctx.writeAndFlush(writeToDatagram(encryptPacket(packet, encryption, ctx.alloc()), receiver));
    }

    private static SimpleBuffer encryptPacket(Packet packet, Encryption encryption, ByteBufAllocator alloc) {
        SimpleBuffer buffer = SimpleBuffer.allocate(64, alloc);
        buffer.putString(PacketRegistry.getInstance().getId(packet.getType()));
        packet.deconstruct(buffer);
        buffer.encrypt(encryption);
        return buffer;
    }

    private static DatagramPacket writeToDatagram(SimpleBuffer buffer, InetSocketAddress address) {
        return new DatagramPacket(buffer.getBuffer(), address);
    }

}
