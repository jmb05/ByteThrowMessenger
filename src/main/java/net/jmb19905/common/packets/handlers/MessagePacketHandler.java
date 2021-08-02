package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.MessagePacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.server.Server;
import net.jmb19905.server.ServerHandler;
import net.jmb19905.server.ServerPacketHandler;

public class MessagePacketHandler extends PacketHandler<MessagePacket>{

    @Override
    public void handleOnServer(MessagePacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) {
        if(connection.getName().equals(packet.message.sender())) {
            String clientName = connection.getName();
            String peerName = packet.message.receiver();
            if (!connection.getName().isBlank()) {
                Chat chat = Server.getChats(clientName, peerName);
                if (chat != null) {
                    if (chat.isActive()) {
                        chat.addMessage(packet.message);
                        ServerPacketHandler.sendPacketToPeer(peerName, packet, handler);
                        Logger.log("Sent message to recipient: " + peerName, Logger.Level.TRACE);
                    } else {
                        sendFail(channel, "message", "Cannot send Message " + peerName + " is offline!", connection);
                    }
                } else {
                    sendFail(channel, "message", "Cannot send Message Chat with: " + peerName + " doesn't exist!", connection);
                }
            } else {
                Logger.log("Client is trying to communicate but isn't logged in!", Logger.Level.WARN);
            }
        }else {
            Logger.log("Received Message with wrong Sender!", Logger.Level.WARN);
        }
    }

    @Override
    public void handleOnClient(MessagePacket packet, EncryptedConnection encryption, Channel channel) {
        String sender = packet.message.sender();
        String receiver = packet.message.receiver();
        String encryptedMessage = packet.message.message();
        if(receiver.equals(ClientMain.client.name)) {
            Chat chat = ClientMain.client.getChat(sender);
            if (chat != null) {
                String decryptedMessage = EncryptionUtility.decryptString(chat.encryption, encryptedMessage);
                chat.addMessage(new Chat.Message(sender, receiver, decryptedMessage));
                ClientMain.window.appendLine("<" + sender + "> " + decryptedMessage);
            }else {
                Logger.log("Received Packet from unknown user", Logger.Level.WARN);
            }
        }else {
            Logger.log("Received Packet destined for someone else", Logger.Level.WARN);
        }
    }
}
