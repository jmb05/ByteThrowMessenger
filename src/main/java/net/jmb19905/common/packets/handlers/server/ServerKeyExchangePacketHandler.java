package net.jmb19905.common.packets.handlers.server;

import io.netty.channel.Channel;
import net.jmb19905.common.Version;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.KeyExchangePacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.StartServer;
import net.jmb19905.server.networking.ServerHandler;

import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class ServerKeyExchangePacketHandler extends ServerPacketHandler<KeyExchangePacket>{

    public ServerKeyExchangePacketHandler(KeyExchangePacket packet) {
        super(packet);
    }

    @Override
    public void handle(ServerHandler serverHandler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        Version packetVersion = new Version(packet.version);
        if(packetVersion.isInCompatible(StartServer.version)){
            sendFail(channel, "version", "client_outdated", "", connection);
            Logger.log("Client tried to connect with outdated version: " + packet.version + " current version: " + StartServer.version, Logger.Level.WARN);
            return;
        }

        byte[] clientEncodedPublicKey = packet.key;

        PublicKey clientPublicKey = null;
        try {
            clientPublicKey = EncryptionUtility.createPublicKeyFromData(clientEncodedPublicKey);
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.ERROR);
        }
        connection.encryption.setReceiverPublicKey(clientPublicKey);

        Logger.log("Connection to Client: " + channel.remoteAddress() + " is encrypted", Logger.Level.INFO);

        //change the key transferred in the packet to the server's PublicKey so the packet can be reused
        packet.key = connection.encryption.getPublicKey().getEncoded();

        Logger.log("Sending packet " + packet + " to " + channel.remoteAddress() , Logger.Level.TRACE);
        NetworkingUtility.sendPacket(packet, channel, null);
    }
}
