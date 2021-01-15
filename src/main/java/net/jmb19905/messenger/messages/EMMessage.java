package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;

public abstract class EMMessage {

    public abstract void handleOnClient(Connection connection) throws UnsupportedSideException;

    public abstract void handleOnServer(Connection connection) throws UnsupportedSideException;

}
