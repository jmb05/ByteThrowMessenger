package net.jmb19905.bytethrow.common.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.jmb19905.bytethrow.common.packets.FailPacket;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.jmbnetty.utility.NetworkUtility;

public class NetworkingUtility {

    public static ChannelFuture sendPacket(Packet packet, Channel channel, Encryption encryption){
        return NetworkUtility.sendTcp(channel, packet, encryption);
    }

    /**
     * Send a Packet to the client to tell him that something failed
     * @param cause the cause of the fail e.g. login or register
     * @param message the message displayed to the client
     */
    public static ChannelFuture sendFail(Channel channel, String cause, String message, String extra, TcpServerHandler handler) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        failPacket.extra = extra;
        return NetworkingUtility.sendPacket(failPacket, channel, handler.getEncryption());
    }

}
