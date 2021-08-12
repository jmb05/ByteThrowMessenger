package net.jmb19905.common.packets;

import net.jmb19905.common.packets.handlers.client.ClientConnectPacketHandler;
import net.jmb19905.common.packets.handlers.client.ClientPacketHandler;
import net.jmb19905.common.packets.handlers.server.ServerConnectPacketHandler;
import net.jmb19905.common.packets.handlers.server.ServerPacketHandler;
import net.jmb19905.common.util.SerializationUtility;

import java.nio.charset.StandardCharsets;

public class ConnectPacket extends Packet{

    public String name;
    public byte[] key;
    public ConnectType connectType;

    public ConnectPacket() {
        super("connect");
    }

    @Override
    public void construct(byte[] data) {
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        String[] parts = dataAsString.split("\\|");
        name = parts[1];
        key = SerializationUtility.decodeBinary(parts[2]);
        int typeInt = Integer.parseInt(parts[3]);
        if(typeInt == 0){
            connectType = ConnectType.FIRST_CONNECT;
        }else if(typeInt == 1){
            connectType = ConnectType.REPLY_CONNECT;
        }else if(typeInt == 2){
            connectType = ConnectType.FIRST_RECONNECT;
        }else if(typeInt == 3){
            connectType = ConnectType.REPLY_RECONNECT;
        }
    }

    @Override
    public byte[] deconstruct() {
        String encodedKey = SerializationUtility.encodeBinary(key);
        return (getId() + "|" + name + "|" + encodedKey + "|" + connectType.typeInt).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ServerPacketHandler<? extends Packet> getServerPacketHandler() {
        return new ServerConnectPacketHandler(this);
    }

    @Override
    public ClientPacketHandler<? extends Packet> getClientPacketHandler() {
        return new ClientConnectPacketHandler(this);
    }

    public enum ConnectType{
        FIRST_CONNECT(0), REPLY_CONNECT(1), FIRST_RECONNECT(2), REPLY_RECONNECT(3);

        private final int typeInt;

        ConnectType(int typeInt){
            this.typeInt = typeInt;
        }
    }

}
