package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.FileMetaPacketHandler;
import net.jmb19905.common.packets.handlers.PacketHandler;

import java.nio.charset.StandardCharsets;

public class FileMetaPacket extends Packet{

    public String meta;
    public int fileLength;
    public byte[] fileData;

    public FileMetaPacket() {
        super("file_meta");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        meta = parts[1];
        fileLength = Integer.parseInt(parts[2]);
        fileData = parts[3].getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] deconstruct() {
        return (getId() + "|" + meta + "|" + fileLength + "|" + new String(fileData, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Packet> PacketHandler<T> getPacketHandler() {
        return (PacketHandler<T>) new FileMetaPacketHandler();
    }
}
