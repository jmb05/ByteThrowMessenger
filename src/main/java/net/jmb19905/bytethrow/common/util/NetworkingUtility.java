package net.jmb19905.bytethrow.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.jmb19905.bytethrow.common.packets.FailPacket;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;

public class NetworkingUtility {

    public static ChannelFuture sendPacket(Packet packet, Channel channel, Encryption encryption){
        final ByteBuf buffer = channel.alloc().buffer();
        byte[] data;
        if(encryption == null || !encryption.isUsable()) {
            data = packet.deconstruct();
        }else {
            data = encryption.encrypt(packet.deconstruct());
        }
        buffer.writeBytes(data);
        return channel.writeAndFlush(buffer);
    }

    /**
     * Send a Packet to the client to tell him that something failed
     * @param cause the cause of the fail e.g. login or register
     * @param message the message displayed to the client
     */
    public static ChannelFuture sendFail(Channel channel, String cause, String message, String extra, TcpServerConnection connection) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        failPacket.extra = extra;
        return NetworkingUtility.sendPacket(failPacket, channel, connection.getEncryption());
    }

}
