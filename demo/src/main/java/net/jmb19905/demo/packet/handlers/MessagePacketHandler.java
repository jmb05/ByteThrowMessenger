package net.jmb19905.demo.packet.handlers;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.demo.Demo;
import net.jmb19905.demo.packet.MessagePacket;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class MessagePacketHandler extends PacketHandler {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet) {
        MessagePacket messagePacket = (MessagePacket) packet;
        Demo.manager.appendMessage(messagePacket.message);
        Logger.info("Received Message: " + messagePacket.message);
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, Packet packet) {
        MessagePacket messagePacket = (MessagePacket) packet;
        Demo.manager.appendMessage(messagePacket.message);
        Logger.info("Received Message: " + messagePacket.message);
    }
}
