package net.jmb19905.util.events;

import org.jetbrains.annotations.NotNull;

import java.util.EventObject;

public abstract class Event<EC extends EventContext> extends EventObject {
    private final EC ctx;
    private final String id;

    public Event(@NotNull EC ctx, String id) {
        super(ctx.getSource());
        this.ctx = ctx;
        this.id = id;
    }

    public EC getContext() {
        return ctx;
    }

    public String getId() {
        return id;
    }
}
