package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.FileMetaPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.server.ServerHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileMetaPacketHandler extends PacketHandler<FileMetaPacket>{

    @Override
    public void handleOnServer(FileMetaPacket packet, ServerHandler serverHandler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        if(packet.fileLength == packet.fileData.length) {
            File file = new File(packet.meta);
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(packet.fileData);
                stream.flush();
            } catch (IOException e) {
                Logger.log(e, Logger.Level.ERROR);
            }
        }else {
            Logger.log("File not received! Data was lost!", Logger.Level.WARN);
        }
    }

    @Override
    public void handleOnClient(FileMetaPacket packet, EncryptedConnection encryption, Channel channel) throws IllegalSideException {
        if(packet.fileLength == packet.fileData.length) {
            File file = new File(packet.meta);
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(packet.fileData);
                stream.flush();
            } catch (IOException e) {
                Logger.log(e, Logger.Level.ERROR);
            }
        }else {
            Logger.log("File not received! Data was lost!", Logger.Level.WARN);
        }
    }
}
