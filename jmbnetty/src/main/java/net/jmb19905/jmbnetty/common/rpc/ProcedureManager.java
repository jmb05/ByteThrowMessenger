package net.jmb19905.jmbnetty.common.rpc;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import net.jmb19905.util.AsynchronousInitializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ProcedureManager {

    private final AtomicLong seqCounter = new AtomicLong();
    private final ClassToInstanceMap<RequestHandler<?>> handlerMap;

    public ProcedureManager() {
        handlerMap = MutableClassToInstanceMap.create();
    }

    public <S extends Response> long addWaiting(Class<S> resClass, AsynchronousInitializer<S> init) {
        long seq = seqCounter.incrementAndGet();
        RequestHandler<S> handler = (RequestHandler<S>) handlerMap.get(resClass);
        if (handler == null) {
            handler = new RequestHandler<>();
            //handlerMap.putInstance(resClass, handler);
        }
        return 0;
    }

    public <S extends Response> void fulfillWaiting(long seq, S response) {
        //var wrapper = waitingRequests.get(seq);
        //var clazz = wrapper.clazz();
        //var res = wrapper.init();
        //res.init(clazz.cast(response));
    }

    private static class RequestHandler<S extends Response> {

        private Map<Long, AsynchronousInitializer<S>> map;

        RequestHandler() {
            map = new HashMap<>();
        }



    }

}
