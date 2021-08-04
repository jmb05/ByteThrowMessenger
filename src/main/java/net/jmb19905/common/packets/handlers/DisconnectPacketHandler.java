package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.DisconnectPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.server.ServerHandler;

public class DisconnectPacketHandler extends PacketHandler<DisconnectPacket> {

    @Override
    public void handleOnServer(DisconnectPacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        throw new IllegalSideException("Received DisconnectPacket on the Server");
    }

    @Override
    public void handleOnClient(DisconnectPacket packet, EncryptedConnection encryption, Channel channel) {
        String peerName = packet.name;
        Chat chat = ClientMain.client.getChat(peerName);
        if(chat != null){
            chat.setActive(false);
            ClientMain.window.setPeerStatus(peerName, false);
            ClientMain.window.append(peerName, ClientMain.window.getBold());
            ClientMain.window.append(" disconnected", null);
            ClientMain.window.newLine();
        }else {
            Logger.log("Received invalid DisconnectPacket", Logger.Level.WARN);
        }
    }
}
