package net.jmb19905.client;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.common.packets.Packet;

public record ClientPacketHandler(ClientHandler clientHandler) {

    public void handlePacket(ChannelHandlerContext ctx, Packet packet) {
        packet.getPacketHandler().handleOnClient(packet, clientHandler.getEncryption(), ctx.channel());
    }


}
