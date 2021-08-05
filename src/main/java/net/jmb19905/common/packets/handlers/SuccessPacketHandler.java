package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.SuccessPacket;
import net.jmb19905.server.ServerHandler;

public class SuccessPacketHandler extends PacketHandler<SuccessPacket> {

    @Override
    public void handleOnServer(SuccessPacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        throw new IllegalSideException("Received SuccessPacket on the Server");
    }

    @Override
    public void handleOnClient(SuccessPacket packet, EncryptedConnection encryption, Channel channel) {
        if(packet.type.equals("login")){
            ClientMain.window.appendLine("Login successful");
        }else if(packet.type.equals("register")){
            ClientMain.window.appendLine("Register successful");
        }
        ClientMain.window.showLoading(false);
    }
}
