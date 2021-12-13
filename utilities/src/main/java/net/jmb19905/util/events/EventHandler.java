package net.jmb19905.util.events;

import net.jmb19905.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EventHandler<C extends EventContext> {

    private boolean valid = false;
    protected Map<String, EventListenerList<C>> eventListeners = new ConcurrentHashMap<>();
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

    public void addEventListener(EventListener listener) {
        EventListenerList<C> listenerBatch = eventListeners.get(id + ":" + listener.getId());
        if (listenerBatch == null) {
            listenerBatch = new EventListenerList<>();
        }
        listenerBatch.addEvent(listener);
        eventListeners.put(id + ":" + listener.getId(), listenerBatch);
    }

    public <E extends Event<C>> void performEvent(E evt) {
        if (isValid()) {
            Logger.info("Performing Event: " + id + ":" + evt.getId());
            EventListenerList<C> listenerBatch = eventListeners.get(id + ":" + evt.getId());
            if (listenerBatch != null) {
                for (EventListener listener : listenerBatch) {
                    listener.perform(evt);
                }
            }
        } else {
            Logger.warn("Event Handler is not valid");
        }
    }

}
