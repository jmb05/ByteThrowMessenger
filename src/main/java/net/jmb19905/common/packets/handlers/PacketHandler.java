package net.jmb19905.common.packets.handlers;

import io.netty.channel.Channel;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.FailPacket;
import net.jmb19905.common.packets.Packet;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.ServerHandler;

public abstract class PacketHandler<P extends Packet> {

    public abstract <T extends Packet> void handleOnServer(P packet, ServerHandler serverHandler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException;

    public abstract <T extends Packet> void handleOnClient(P packet, EncryptedConnection encryption, Channel channel) throws IllegalSideException;

    /**
     * Send a Packet to the client to tell him that something failed
     * @param cause the cause of the fail e.g. login or register
     * @param message the message displayed to the client
     */
    protected void sendFail(Channel channel, String cause, String message, ServerHandler.ClientConnection connection) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        NetworkingUtility.sendPacket(failPacket, channel, connection.encryption);
    }

}
