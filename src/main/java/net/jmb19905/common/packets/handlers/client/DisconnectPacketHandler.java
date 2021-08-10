package net.jmb19905.common.packets.handlers.client;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.DisconnectPacket;
import net.jmb19905.common.util.Logger;

public class DisconnectPacketHandler extends ClientPacketHandler<DisconnectPacket> {

    public DisconnectPacketHandler(DisconnectPacket packet) {
        super(packet);
    }

    @Override
    public void handle(EncryptedConnection encryption, Channel channel) {
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
