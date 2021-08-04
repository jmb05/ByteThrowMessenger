package net.jmb19905.client;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.Packet;
import net.jmb19905.common.util.Logger;

public record ClientPacketHandler(ClientHandler clientHandler) {

    public void handlePacket(ChannelHandlerContext ctx, Packet packet) {
        try {
            packet.getPacketHandler().handleOnClient(packet, clientHandler.getEncryption(), ctx.channel());
        } catch (IllegalSideException e) {
            Logger.log(e, Logger.Level.WARN);
        }
    }


}
