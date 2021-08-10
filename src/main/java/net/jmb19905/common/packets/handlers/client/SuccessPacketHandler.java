package net.jmb19905.common.packets.handlers.client;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientMain;
import net.jmb19905.client.networking.Client;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.ChatsRequestPacket;
import net.jmb19905.common.packets.SuccessPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;

public class SuccessPacketHandler extends ClientPacketHandler<SuccessPacket> {

    private final SuccessPacket packet;

    public SuccessPacketHandler(SuccessPacket packet) {
        super(packet);
        this.packet = packet;
    }

    @Override
    public void handle(EncryptedConnection encryption, Channel channel) throws IllegalSideException {
        if(packet.type.equals("login")){
            doOnLoginSuccess(encryption, channel);
        }else if(packet.type.equals("register")){
            doOnLoginSuccess(encryption, channel);
        }
        ClientMain.window.showLoading(false);
    }

    private void doOnLoginSuccess(EncryptedConnection encryption, Channel channel){
        if(!packet.confirmIdentity) {
            if(!ClientMain.client.loggedIn) {
                ClientMain.window.appendLine("Login successful");
                ClientMain.client.loggedIn = true;
                ChatsRequestPacket chatsRequestPacket = new ChatsRequestPacket();
                NetworkingUtility.sendPacket(chatsRequestPacket, channel, encryption);
            }else {
                Logger.log("Already logged in", Logger.Level.WARN);
            }
        }else {
            Client.confirmIdentityPacket = packet;
        }
        ClientMain.client.confirmIdentity();
    }
}
