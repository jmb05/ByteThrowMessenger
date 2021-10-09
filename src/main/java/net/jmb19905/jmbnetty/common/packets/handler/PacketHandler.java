package net.jmb19905.jmbnetty.common.packets.handler;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;

public abstract class PacketHandler {

    public abstract void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler handler);

    public abstract void handleOnClient(ChannelHandlerContext ctx, Packet packet, TcpClientHandler handler);

}
