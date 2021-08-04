package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.ChatsPacket;
import net.jmb19905.common.packets.ConnectPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.ServerHandler;

public class ChatsPacketHandler extends PacketHandler<ChatsPacket> {

    @Override
    public void handleOnServer(ChatsPacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        throw new IllegalSideException("Received ChatsPacket on the Server");
    }

    @Override
    public void handleOnClient(ChatsPacket packet, EncryptedConnection encryption, Channel channel) {
        for(String name : packet.names){
            Chat chat = new Chat();
            chat.initClient();
            chat.addClient(ClientMain.client.name);
            chat.addClient(name);

            ClientMain.client.chats.add(chat);

            ConnectPacket connectPacket = new ConnectPacket();
            connectPacket.connectType = ConnectPacket.ConnectType.FIRST_RECONNECT;
            connectPacket.name = name;
            connectPacket.key = chat.encryption.getPublicKey().getEncoded();

            NetworkingUtility.sendPacket(connectPacket, channel, encryption);
            Logger.log("Sent " + connectPacket, Logger.Level.DEBUG);
        }
        ClientMain.window.setPeers(packet.names);
    }
}
