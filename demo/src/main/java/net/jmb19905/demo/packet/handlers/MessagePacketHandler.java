package net.jmb19905.demo.packet.handlers;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.demo.Demo;
import net.jmb19905.demo.packet.MessagePacket;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.util.Logger;

public class MessagePacketHandler extends PacketHandler<MessagePacket> {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, MessagePacket packet) {
        Demo.manager.appendMessage(packet.message);
        Logger.info("Received Message: " + packet.message);
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, MessagePacket packet) {
        Demo.manager.appendMessage(packet.message);
        Logger.info("Received Message: " + packet.message);
    }
}
