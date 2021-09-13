package net.jmb19905.common.packets.handlers.client;

import io.netty.channel.Channel;
import net.jmb19905.client.StartClient;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.ChatsPacket;
import net.jmb19905.common.packets.ConnectPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;

public class ChatsPacketHandler extends ClientPacketHandler<ChatsPacket> {

    public ChatsPacketHandler(ChatsPacket packet){
        super(packet);
    }

    @Override
    public void handle(EncryptedConnection encryption, Channel channel) {
        Logger.log(packet.toString(), Logger.Level.DEBUG);
        if(packet.update) {
            StartClient.client.chats.clear();
        }
        for(String name : packet.names){
            Chat chat = new Chat();
            chat.initClient();
            chat.addClient(StartClient.client.name);
            chat.addClient(name);

            StartClient.client.chats.add(chat);

            if(!packet.update) {
                ConnectPacket connectPacket = new ConnectPacket();
                connectPacket.connectType = ConnectPacket.ConnectType.FIRST_RECONNECT;
                connectPacket.name = name;
                connectPacket.key = chat.encryption.getPublicKey().getEncoded();

                NetworkingUtility.sendPacket(connectPacket, channel, encryption);
                Logger.log("Sent " + connectPacket, Logger.Level.TRACE);
            }
        }
        StartClient.window.setPeers(packet.names);
    }
}
