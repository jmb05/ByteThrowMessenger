package net.jmb19905.jmbnetty.utility;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;

import java.net.InetSocketAddress;

@SuppressWarnings("UnusedReturnValue")
public class NetworkUtility {

    public static ChannelFuture sendTcp(SocketChannel channel, Packet packet, Encryption encryption){
        return channel.writeAndFlush(writeToByteBuf(encryptPacket(packet, encryption), channel.alloc()));
    }

    public static ChannelFuture sendTcp(ChannelHandlerContext ctx, Packet packet, Encryption encryption){
        return ctx.writeAndFlush(writeToByteBuf(encryptPacket(packet, encryption), ctx.alloc()));
    }

    public static ChannelFuture sendUdp(NioDatagramChannel channel, Packet packet, InetSocketAddress receiver, Encryption encryption){
        return channel.writeAndFlush(writeToDatagram(encryptPacket(packet, encryption), channel.alloc(), receiver));
    }

    public static ChannelFuture sendUdp(ChannelHandlerContext ctx, Packet packet, InetSocketAddress receiver, Encryption encryption){
        return ctx.writeAndFlush(writeToDatagram(encryptPacket(packet, encryption), ctx.alloc(), receiver));
    }

    private static byte[] encryptPacket(Packet packet, Encryption encryption){
        byte[] deconstructedPacket = packet.deconstruct();
        byte[] encryptedPacket = deconstructedPacket;
        if(encryption != null && encryption.isUsable()){
            encryptedPacket = encryption.encrypt(deconstructedPacket);
        }
        return encryptedPacket;
    }

    private static ByteBuf writeToByteBuf(byte[] data, ByteBufAllocator alloc){
        ByteBuf byteBuf = alloc.buffer(data.length);
        byteBuf.writeBytes(data);
        return byteBuf;
    }

    private static DatagramPacket writeToDatagram(byte[] data, ByteBufAllocator alloc, InetSocketAddress address){
        return new DatagramPacket(writeToByteBuf(data, alloc), address);
    }

}
