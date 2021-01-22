package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;

public class KeepAlivePacket extends BTMPacket{

    public KeepAlivePacket(){}

    @Override
    public void handleOnClient(Connection connection){/*Doesn't handle it because it does not carry and information*/}

    @Override
    public void handleOnServer(Connection connection){
        connection.sendTCP(this);
    }
}
