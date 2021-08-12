package net.jmb19905.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.Packet;

import java.util.Base64;

public class NetworkingUtility {

    public static ChannelFuture sendPacket(Packet packet, Channel channel, EncryptedConnection encryption){
        final ByteBuf buffer = channel.alloc().buffer();
        byte[] data;
        if(encryption == null || !encryption.isUsable()) {
            data = packet.deconstruct();
        }else {
            data = encryption.encrypt(packet.deconstruct());
        }
        byte[] encodedData = Base64.getEncoder().encode(data);
        buffer.writeBytes(encodedData);
        return channel.writeAndFlush(buffer);
    }

}
