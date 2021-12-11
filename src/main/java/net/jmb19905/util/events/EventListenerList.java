package net.jmb19905.util.events;

import java.util.ArrayList;

public class EventListenerList<E extends Event<? extends EventContext>> extends ArrayList<EventListener<E>> {
    @SuppressWarnings("unchecked")
    public void add(EventListener<? extends Event<? extends EventContext>> listener) {
        super.add((EventListener<E>) listener);
    }
}