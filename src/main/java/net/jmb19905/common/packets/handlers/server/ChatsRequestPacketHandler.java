package net.jmb19905.common.packets.handlers.server;

import io.netty.channel.Channel;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.ChatsPacket;
import net.jmb19905.common.packets.ChatsRequestPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.networking.Server;
import net.jmb19905.server.networking.ServerHandler;

import java.util.ArrayList;
import java.util.List;

public class ChatsRequestPacketHandler extends ServerPacketHandler<ChatsRequestPacket> {

    public ChatsRequestPacketHandler(ChatsRequestPacket packet) {
        super(packet);
    }

    @Override
    public void handle(ServerHandler serverHandler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
        String clientName = connection.getName();
        ChatsPacket packet = new ChatsPacket();
        packet.names = getPeerNames(clientName);

        Logger.log("Sending packet " + packet + " to " + channel.remoteAddress(), Logger.Level.TRACE);
        NetworkingUtility.sendPacket(packet, channel, connection.encryption);
    }

    private String[] getPeerNames(String clientName) {
        List<String> names = new ArrayList<>();
        for(int i = 0; i< Server.chats.size(); i++){
            List<String> chatParticipants = Server.chats.get(i).getClients();
            if(chatParticipants.contains(clientName)){
                for(String otherName : chatParticipants) {
                    if(!otherName.equals(clientName)) {
                        names.add(otherName);
                    }
                }
            }
        }
        return names.toArray(new String[0]);
    }

}
