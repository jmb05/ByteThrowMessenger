package net.jmb19905.jmbnetty.common.connection.event;

import net.jmb19905.jmbnetty.common.connection.event.abstr.NetworkEventListener;

public interface ConnectedEventListener extends NetworkEventListener<ConnectedEvent> {
    @Override
    default String getId() {
        return "connected";
    }
}