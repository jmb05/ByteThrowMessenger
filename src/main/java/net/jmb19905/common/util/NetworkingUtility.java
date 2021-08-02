package net.jmb19905.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.Packet;

public class NetworkingUtility {

    public static void sendPacket(Packet packet, Channel channel, EncryptedConnection encryption){
        final ByteBuf buffer = channel.alloc().buffer();
        byte[] data;
        if(encryption == null) {
            data = packet.deconstruct();
        }else {
            data = encryption.encrypt(packet.deconstruct());
        }
        buffer.writeBytes(data);
        channel.writeAndFlush(buffer);
    }

}
