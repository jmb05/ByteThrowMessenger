package net.jmb19905.jmbnetty.common.connection.event.abstr;

import java.util.EventListener;

public interface NetworkEventListener<E extends NetworkEvent> extends EventListener {
    void perform(E evt);
    String getId();
}