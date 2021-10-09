/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.packets.MessagePacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.networking.ServerManager;
import net.jmb19905.util.Logger;

public class MessagePacketHandler extends PacketHandler {

    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler serverHandler) {
        MessagePacket messagePacket = (MessagePacket) packet;
        Chat.Message message = messagePacket.message;
        TcpServerConnection connection = (TcpServerConnection) serverHandler.getConnection();
        ServerManager manager = StartServer.manager;
        String name = "connection.getName()";
        if(name.equals(message.sender())) {
            String peerName = message.receiver();
            if (!name.isBlank()) {
                Chat chat = manager.getChats(name, peerName);
                if (chat != null) {
                    if (chat.isActive()) {
                        chat.addMessage(message);
                        manager.sendPacketToPeer(peerName, messagePacket, serverHandler);
                        Logger.trace("Sent message to recipient: " + peerName);
                    } else {
                        NetworkingUtility.sendFail(ctx.channel(), "message", "peer_offline", peerName, connection);
                    }
                } else {
                    NetworkingUtility.sendFail(ctx.channel(), "message", "no_such_chat", peerName, connection);
                }
            } else {
                Logger.warn("Client is trying to communicate but isn't logged in!");
            }
        }else {
            Logger.warn("Received Message with wrong Sender!");
        }
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) {
        MessagePacket messagePacket = (MessagePacket) packet;
        Chat.Message message = messagePacket.message;
        String sender = message.sender();
        String receiver = message.receiver();
        String encryptedMessage = message.message();
        if(receiver.equals(StartClient.manager.name)) {
            Chat chat = StartClient.manager.getChat(sender);
            if (chat != null) {
                String decryptedMessage = EncryptionUtility.decryptString(chat.encryption, encryptedMessage);
                chat.addMessage(new Chat.Message(sender, receiver, decryptedMessage));
                StartClient.window.appendMessage(sender, decryptedMessage);
                //Notify.create().title("ByteThrow Messenger").text("[" + sender + "] " + decryptedMessage).darkStyle().show();
            }else {
                Logger.warn("Received Packet from unknown user");
            }
        }else {
            Logger.warn("Received Packet destined for someone else");
        }
    }
}
