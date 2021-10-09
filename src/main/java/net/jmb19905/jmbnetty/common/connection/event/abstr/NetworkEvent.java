package net.jmb19905.jmbnetty.common.connection.event.abstr;

import java.util.EventObject;

/**
 * Holds the Objects that may be needed for a NetworkEvent to be handled
 */
public abstract class NetworkEvent extends EventObject {
    public NetworkEvent(Object source) {
        super(source);
    }
}
