/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

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
        addTask(in -> {
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

            byte[] data;
            int lastEnd = 0;
            for(int i=0;i<rawData.length;i++){
                if(rawData[i] == '%'){
                    data = Arrays.copyOfRange(rawData, lastEnd, i);
                    lastEnd = i + 1;

                    for(DecoderTask task : tasks){
                        data = task.decode(data);
                    }

                    if(data == null || data.length < 1){
                        Logger.warn("Ignoring faulty Packet");
                        return;
                    }

                    Packet packet = PacketUtil.construct(data);
                    Logger.trace("Decoded Packet: " + packet);
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
