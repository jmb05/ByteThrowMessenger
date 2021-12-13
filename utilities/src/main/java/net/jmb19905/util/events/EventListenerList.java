package net.jmb19905.util.events;

import java.util.ArrayList;

public class EventListenerList<C extends EventContext> extends ArrayList<EventListener<? extends Event<C>>> {
    public void addEvent(EventListener<? extends Event<C>> listener) {
        super.add(listener);
    }
}