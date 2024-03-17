package net.jmb19905.jmbnetty.common.rpc;

import io.netty.channel.Channel;
import net.jmb19905.jmbnetty.utility.NetworkUtility;
import net.jmb19905.util.AsynchronousInitializer;

import java.util.concurrent.Future;

public class Procedure<T extends Request, S extends Response> {

    private final ProcedureType<? extends Procedure<T, S>> procType;

    public Procedure(ProcedureType<? extends Procedure<T, S>> procType) {
        this.procType = procType;
    }

    public String getId() {
        return ProcedureRegistry.getInstance().getId(procType);
    }

    public AsynchronousInitializer<S> request(Class<T> reqClass, T req, Channel channel, ProcedureManager manager) {
        var id = ProcedureRegistry.getInstance().getId(procType);
        ReqProcPacket packet = new ReqProcPacket(id, reqClass, req);
        NetworkUtility.sendTcp(channel, packet, null);
        AsynchronousInitializer<S> init = new AsynchronousInitializer<>();

        return init;
    }

}
