package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.bytethrow.common.util.SerializationUtility;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class ConnectPacket extends Packet {

    private static final String ID = "connect";

    public String name;
    public byte[] key;
    public ConnectType connectType;

    public ConnectPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] parts) {
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
        return (ID + "|" + name + "|" + encodedKey + "|" + connectType.typeInt).getBytes(StandardCharsets.UTF_8);
    }

    public enum ConnectType{
        FIRST_CONNECT(0), REPLY_CONNECT(1), FIRST_RECONNECT(2), REPLY_RECONNECT(3);

        private final int typeInt;

        ConnectType(int typeInt){
            this.typeInt = typeInt;
        }
    }

}
