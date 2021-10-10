package net.jmb19905.jmbnetty.common.connection;

import net.jmb19905.jmbnetty.common.connection.event.ConnectedEventListener;
import net.jmb19905.jmbnetty.common.connection.event.DisconnectedEventListener;
import net.jmb19905.jmbnetty.common.connection.event.ErrorEventListener;
import net.jmb19905.jmbnetty.common.connection.event.abstr.NetworkEvent;
import net.jmb19905.jmbnetty.common.connection.event.abstr.NetworkEventListener;
import net.jmb19905.util.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class AbstractConnection implements IConnection, Runnable{

    protected int port;
    protected Thread thread;

    protected Map<String, EventListenerList<? extends NetworkEvent>> eventListeners = new ConcurrentHashMap<>();

    protected boolean closed = false;

    public AbstractConnection(){
        this.thread = new Thread(this);
    }

    @Override
    public void start() {
        this.thread.start();
    }

    @Override
    public void stop() {
        this.thread.interrupt();
    }

    public void addConnectedEventListener(ConnectedEventListener listener){
        addEventListener(listener);
    }

    public void addErrorEventListener(ErrorEventListener listener){
        addEventListener(listener);
    }

    public void addDisconnectedEventListener(DisconnectedEventListener listener){
        addEventListener(listener);
    }

    public void addEventListener(NetworkEventListener<? extends NetworkEvent> listener){
        EventListenerList<? extends NetworkEvent> listenerBatch = eventListeners.get(listener.getId());
        if(listenerBatch == null){
            listenerBatch = new EventListenerList<>();
        }
        listenerBatch.add(listener);
        eventListeners.put(listener.getId(), listenerBatch);
    }

    @SuppressWarnings("unchecked")
    public <E extends NetworkEvent> void performEvent(String id, Supplier<? extends NetworkEvent> evt){
        if(!closed) {
            EventListenerList<E> listenerBatch = (EventListenerList<E>) eventListeners.get(id);
            if (listenerBatch != null) {
                for (NetworkEventListener<E> listener : listenerBatch) {
                    listener.perform((E) evt.get());
                }
            }
        }
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public int getPort(){
        return port;
    }

    @SuppressWarnings("unchecked")
    private static class EventListenerList<E extends NetworkEvent> extends ArrayList<NetworkEventListener<E>>{
        public void add(NetworkEventListener<? extends NetworkEvent> listener) {
            super.add((NetworkEventListener<E>) listener);
        }
    }

    public void markClosed(){
        closed = true;
        Logger.info("Connection marked as closed");
    }

    public boolean isClosed() {
        return closed;
    }
}