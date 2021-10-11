package net.jmb19905.jmbnetty.common.connection.event;

import net.jmb19905.jmbnetty.common.connection.event.abstr.NetworkEvent;

public class ErrorEvent extends NetworkEvent {

    private final Throwable cause;

    public ErrorEvent(Object source, Throwable cause) {
        super(source);
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }
}
