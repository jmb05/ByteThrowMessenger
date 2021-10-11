package net.jmb19905.jmbnetty.common.connection.event;

import net.jmb19905.jmbnetty.common.connection.event.abstr.NetworkEventListener;

public interface ErrorEventListener extends NetworkEventListener<ErrorEvent> {
    @Override
    default String getId(){
        return "error";
    }
}
