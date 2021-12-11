package net.jmb19905.util.events;

public interface EventListener<E extends Event<? extends EventContext>> extends java.util.EventListener {
    void perform(E evt);
    String getId();
}
