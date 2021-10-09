package net.jmb19905.jmbnetty.common.connection.event;

import net.jmb19905.jmbnetty.common.connection.event.abstr.NetworkEventListener;

public interface DisconnectedEventListener extends NetworkEventListener<DisconnectedEvent> {
    @Override
    default String getId() {
        return "disconnected";
    }
}