package net.jmb19905.common.packets.handlers.client;

import io.netty.channel.Channel;
import net.jmb19905.client.StartClient;
import net.jmb19905.common.Version;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.KeyExchangePacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;

import javax.swing.*;
import java.security.spec.InvalidKeySpecException;

public class ClientKeyExchangePacketHandler extends ClientPacketHandler<KeyExchangePacket>{

    public ClientKeyExchangePacketHandler(KeyExchangePacket packet) {
        super(packet);
    }

    @Override
    public void handle(EncryptedConnection encryption, Channel channel) {
        Version packetVersion = new Version(packet.version);
        if(packetVersion.isInCompatible(StartClient.version)){
            JOptionPane.showMessageDialog(StartClient.window,"Client is outdated!", "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
            StartClient.window.appendLine("Connection to Server encrypted");
            net.jmb19905.client.networking.ClientHandler.login(channel, encryption);
        }catch (InvalidKeySpecException e){
            Logger.log(e, Logger.Level.FATAL);
            StartClient.exit(-1);
        }
    }
}
