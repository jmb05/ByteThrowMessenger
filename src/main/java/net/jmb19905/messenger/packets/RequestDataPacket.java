package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;

public class RequestDataPacket extends BTMPacket{

    public String username;

    public RequestDataPacket(){}

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("RequestDataPacket received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {

    }
}
