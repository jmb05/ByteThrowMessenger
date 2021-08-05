package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientHandler;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.Version;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.KeyExchangePacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.ServerHandler;
import net.jmb19905.server.StartServer;

import javax.swing.*;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class KeyExchangePacketHandler extends PacketHandler<KeyExchangePacket>{

    @Override
    public void handleOnServer(KeyExchangePacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) {
        Version packetVersion = new Version(packet.version);
        if(packetVersion.isInCompatible(StartServer.version)){
            sendFail(channel, "version", "Client is outdated!", connection);
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

    @Override
    public void handleOnClient(KeyExchangePacket packet, EncryptedConnection encryption, Channel channel) {
        Version packetVersion = new Version(packet.version);
        if(packetVersion.isInCompatible(ClientMain.version)){
            JOptionPane.showMessageDialog(ClientMain.window,"Client is outdated!", "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
            ClientMain.window.appendLine("Connection to Server encrypted");
            ClientHandler.login(channel, encryption);
        }catch (InvalidKeySpecException e){
            Logger.log(e, Logger.Level.FATAL);
            System.exit(-1);
        }
    }

}
