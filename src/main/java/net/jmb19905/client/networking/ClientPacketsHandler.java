package net.jmb19905.client.networking;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.Packet;
import net.jmb19905.common.util.Logger;

public record ClientPacketsHandler(ClientHandler clientHandler) {

    public void handlePacket(ChannelHandlerContext ctx, Packet packet) {
        try {
            packet.getClientPacketHandler().handle(clientHandler.getEncryption(), ctx.channel());
        } catch (IllegalSideException e) {
            Logger.log(e, Logger.Level.WARN);
        }
    }


}
