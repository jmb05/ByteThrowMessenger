package net.jmb19905.jmbnetty.common.rpc;

import net.jmb19905.jmbnetty.common.buffer.SimpleBuffer;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;
import org.jetbrains.annotations.NotNull;

public class ReqProcPacket extends Packet {

    public static final String ID = "req_proc";

    private String id;
    private final Class<? extends Request> reqClass;
    private Request request;

    public <R extends Request> ReqProcPacket(String id, @NotNull Class<R> reqClass, R request) {
        super(PacketRegistry.getInstance().getPacketType(ID));
        this.id = id;
        this.reqClass = reqClass;
        this.request = request;
    }

    @Override
    public void construct(SimpleBuffer buffer) {
        id = buffer.getString();
        request = buffer.get(reqClass);
    }

    @Override
    public void deconstruct(SimpleBuffer buffer) {
        buffer.putString(id);
        buffer.put(request);
    }
}
