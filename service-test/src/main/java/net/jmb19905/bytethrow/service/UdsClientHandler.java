package net.jmb19905.bytethrow.service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class UdsClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        int length = buffer.readInt();
        String s = (String) buffer.readCharSequence(length, StandardCharsets.UTF_8);
        System.out.println("Received message: " + s);
    }
}
