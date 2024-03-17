package net.jmb19905.jmbnetty.common.rpc;

import net.jmb19905.jmbnetty.common.buffer.SimpleBuffer;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;
import org.jetbrains.annotations.NotNull;

public class ResProcPacket extends Packet {

    private static final String ID = "res_proc";

    private String id;
    private final Class<? extends Response> resClass;
    private Response response;

    protected ResProcPacket(String id, @NotNull Class<? extends Response> resClass, Response response) {
        super(PacketRegistry.getInstance().getPacketType(ID));
        this.id = id;
        this.resClass = resClass;
        this.response = response;
    }

    @Override
    public void construct(SimpleBuffer buffer) {
        id = buffer.getString();
        response = buffer.get(resClass);
    }

    @Override
    public void deconstruct(SimpleBuffer buffer) {
        buffer.putString(id);
        buffer.put(response);
    }
}
