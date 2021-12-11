package net.jmb19905.demo.managers;

import io.netty.channel.Channel;
import net.jmb19905.demo.gui.Window;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import org.jetbrains.annotations.Nullable;

public abstract class EndpointManager {

    protected abstract void start();
    public abstract void stop();

    public abstract void sendMessage(String message, Channel channel, Encryption encryption);

    @Nullable
    public abstract Window getWindow();

    public void appendMessage(String message) {
        Window window = getWindow();
        if(window != null) {
            window.appendMessage("Other", message);
        }
    }

}
