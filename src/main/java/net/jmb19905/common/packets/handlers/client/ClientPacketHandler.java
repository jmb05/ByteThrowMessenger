package net.jmb19905.common.packets.handlers.client;

import io.netty.channel.Channel;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.Packet;

public abstract class ClientPacketHandler<P extends Packet> {

    protected P packet;

    public ClientPacketHandler(P packet){
        this.packet = packet;
    }

    public abstract void handle(EncryptedConnection encryption, Channel channel) throws IllegalSideException;

}
