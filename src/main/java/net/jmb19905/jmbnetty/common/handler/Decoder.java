package net.jmb19905.jmbnetty.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketUtil;
import net.jmb19905.util.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Decoder extends ByteToMessageDecoder {

    private final List<DecoderTask> tasks = new ArrayList<>();

    public Decoder(Encryption encryption){
        tasks.add(in -> {
            if(encryption == null || !encryption.isUsable()){
                return in;
            }
            return encryption.decrypt(in);
        });
    }

    public void addTask(DecoderTask task){
        tasks.add(task);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            byte[] rawData = new byte[in.readableBytes()];
            in.readBytes(rawData);

            Logger.debug("Decoding: " + new String(rawData, StandardCharsets.UTF_8));

            for(DecoderTask task : tasks){
                rawData = task.decode(rawData);
            }

            byte[] data;
            int lastEnd = 0;
            for(int i=0;i<rawData.length;i++){
                if(rawData[i] == '%'){
                    data = Arrays.copyOfRange(rawData, lastEnd, i);
                    lastEnd = i + 1;
                    Packet packet = PacketUtil.construct(data);
                    out.add(packet);
                }
            }

            if(out.isEmpty()){
                throw new IllegalStateException("Error decoding: Could not find End of Packet");
            }
        }catch (IllegalStateException e){
            Logger.warn(e);
        }
    }

    public interface DecoderTask{
        byte[] decode(byte[] in);
    }

}
