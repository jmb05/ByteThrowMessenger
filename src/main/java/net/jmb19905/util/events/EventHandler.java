package net.jmb19905.util.events;

import net.jmb19905.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandler<C extends EventContext> {

    private boolean valid = false;
    protected Map<String, EventListenerList<Event<C>>> eventListeners = new ConcurrentHashMap<>();
    private final String id;

    public EventHandler(String id) {
        this.id = id;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public void addEventListener(EventListener<? extends Event<C>> listener) {
        EventListenerList<Event<C>> listenerBatch = eventListeners.get(listener.getId());
        if (listenerBatch == null) {
            listenerBatch = new EventListenerList<>();
        }
        listenerBatch.add(listener);
        eventListeners.put(id + ":" + listener.getId(), listenerBatch);
    }

    public void performEvent(Event<C> evt) {
        if (isValid()) {
            Logger.info("Performing Event: " + id + ":" + evt.getId());
            EventListenerList<Event<C>> listenerBatch = eventListeners.get(id + ":" + evt.getId());
            if (listenerBatch != null) {
                for (EventListener<Event<C>> listener : listenerBatch) {
                    listener.perform(evt);
                }
            }
        } else {
            Logger.warn("Event Handler is not valid");
        }
    }

}
