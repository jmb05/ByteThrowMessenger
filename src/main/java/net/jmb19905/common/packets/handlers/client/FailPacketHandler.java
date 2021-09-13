package net.jmb19905.common.packets.handlers.client;

import io.netty.channel.Channel;
import net.jmb19905.client.StartClient;
import net.jmb19905.client.networking.ClientHandler;
import net.jmb19905.client.util.Localisation;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.FailPacket;

import javax.swing.*;

public class FailPacketHandler extends ClientPacketHandler<FailPacket> {

    public FailPacketHandler(FailPacket packet) {
        super(packet);
    }

    @Override
    public void handle(EncryptedConnection encryption, Channel channel) {
        String cause = packet.cause;
        String message = Localisation.get(packet.message);
        if(!packet.extra.equals(" ")){
            message = Localisation.get(packet.message, packet.extra);
        }
        JOptionPane.showMessageDialog(StartClient.window, message, "", JOptionPane.ERROR_MESSAGE);
        switch (cause.split(":")[0]) {
            case "login" -> ClientHandler.login(channel, encryption);
            case "register" -> ClientHandler.register(channel, encryption);
            case "version" -> StartClient.exit(-1, packet.message, true);
            case "external_disconnect" -> StartClient.exit(0, packet.message, true);
            case "connect" -> {
                String peerName = cause.split(":")[1];
                Chat chat = StartClient.client.getChat(peerName);
                StartClient.client.chats.remove(chat);
                StartClient.window.removePeer(peerName);
            }
        }
    }
}
