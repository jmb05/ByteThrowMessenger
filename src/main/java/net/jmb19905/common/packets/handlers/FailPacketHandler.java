package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientHandler;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.FailPacket;
import net.jmb19905.server.ServerHandler;

import javax.swing.*;

public class FailPacketHandler extends PacketHandler<FailPacket> {

    @Override
    public void handleOnServer(FailPacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        throw new IllegalSideException("Received FailPacket on the Server");
    }

    @Override
    public void handleOnClient(FailPacket packet, EncryptedConnection encryption, Channel channel) {
        String cause = packet.cause;
        JOptionPane.showMessageDialog(ClientMain.window, packet.message, "", JOptionPane.ERROR_MESSAGE);
        if(cause.equals("login")){
            ClientHandler.login(channel, encryption);
        }else if(cause.equals("register")){
            ClientHandler.register(channel, encryption);
        }else if(cause.startsWith("connect")){
            String peerName = cause.split(":")[1];
            Chat chat = ClientMain.client.getChat(peerName);
            ClientMain.client.chats.remove(chat);
        }
    }
}
