package net.jmb19905.jmbnetty.common.connection.event;

import net.jmb19905.jmbnetty.common.connection.event.abstr.NetworkEvent;
import net.jmb19905.jmbnetty.common.handler.AbstractChannelHandler;

public class DisconnectedEvent extends NetworkEvent {
    public DisconnectedEvent(AbstractChannelHandler source) {
        super(source);
    }
}
