package net.jmb19905.demo.packet.handlers;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.demo.Demo;
import net.jmb19905.demo.packet.KeyExchangePacket;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.jmbnetty.utility.NetworkUtility;
import net.jmb19905.util.Logger;

import java.security.PublicKey;
import java.util.Objects;

public class KeyExchangePacketHandler extends PacketHandler<KeyExchangePacket> {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, KeyExchangePacket packet) {
        TcpServerHandler handler = (TcpServerHandler) ctx.handler();

        byte[] key = packet.key;

        PublicKey clientPublicKey = EncryptionUtility.createPublicKeyFromData(key);
        handler.setPublicKey(clientPublicKey);

        Logger.info("Connection Encrypted");
        Objects.requireNonNull(Demo.manager.getWindow()).setFieldEnabled(true);
        Objects.requireNonNull(Demo.manager.getWindow()).appendMessage("", "Encrypted Channel established");

        //change the key transferred in the packet to the server's PublicKey so the packet can be reused
        packet.key = handler.getEncryption().getPublicKey().getEncoded();

        Logger.trace("Sending packet " + packet + " to " + ctx.channel().remoteAddress());
        NetworkUtility.sendTcp(ctx.channel(), packet, null);
    }

    @Override
    public void handleOnClient(ChannelHandlerContext ctx, KeyExchangePacket packet) {
        TcpClientHandler handler = (TcpClientHandler) ctx.handler();
        handler.setPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));

        Logger.info("Connection Encrypted");
        Objects.requireNonNull(Demo.manager.getWindow()).setFieldEnabled(true);
        Objects.requireNonNull(Demo.manager.getWindow()).appendMessage("", "Encrypted Channel established");
    }
}
