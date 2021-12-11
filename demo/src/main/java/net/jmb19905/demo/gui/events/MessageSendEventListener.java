package net.jmb19905.demo.gui.events;

import net.jmb19905.util.events.EventListener;

public interface MessageSendEventListener extends EventListener<MessageSendEvent> {
    default String getId() {
        return MessageSendEvent.ID;
    }
 }
