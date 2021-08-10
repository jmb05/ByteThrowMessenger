package net.jmb19905.server.networking;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.jmb19905.common.exception.IllegalPacketSignatureException;
import net.jmb19905.common.packets.Packet;
import net.jmb19905.common.util.Logger;

import java.util.Base64;
import java.util.List;

public class ServerDecoder extends ByteToMessageDecoder {

    private final ServerHandler handler;

    public ServerDecoder(ServerHandler handler){
        this.handler = handler;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            byte[] encryptedData = new byte[in.readableBytes()];
            in.readBytes(encryptedData);
            encryptedData = Base64.getDecoder().decode(encryptedData);
            byte[] data = decryptData(encryptedData);
            Packet packet = Packet.constructPacket(data);
            out.add(packet);
            Logger.log("Decoded Packet: " + packet, Logger.Level.DEBUG);
        } catch (IllegalPacketSignatureException e) {
            Logger.log(e, "IllegalPacketSignatureException: Unexpected Packet signature", Logger.Level.ERROR);
        } catch (IllegalArgumentException ignored){ }
    }

    /**
     * Decrypts a byte-array received from the client if connection is already encrypted
     * @param encryptedArray the byte-array that might be encrypted
     * @return a decrypted array of bytes
     */
    private byte[] decryptData(byte[] encryptedArray) {
        byte[] array;
        if(handler.getConnection().encryption.isUsable()){
            array = handler.getConnection().encryption.decrypt(encryptedArray);
        }else {
            array = new byte[encryptedArray.length];
            System.arraycopy(encryptedArray, 0, array, 0, array.length);
        }
        return array;
    }

}
