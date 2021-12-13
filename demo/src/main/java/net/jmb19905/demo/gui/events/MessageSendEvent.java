package net.jmb19905.demo.gui.events;

/**
 * Called when a message send functionality is executed
 */
public class MessageSendEvent extends DemoGuiEvent {

    public static final String ID = "message_send";

    private final String message;

    public MessageSendEvent(DemoGuiEventContext ctx, String message) {
        super(ctx, ID);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
