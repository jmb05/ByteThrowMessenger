package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;

public abstract class EMMessage {

    /**
     *
     * Tells the Client what to do when a Message of this type was received
     *
     * @param connection the Connection that the Message was received on
     * @throws UnsupportedSideException if the Message was received on the wrong side
     */
    public abstract void handleOnClient(Connection connection) throws UnsupportedSideException;

    /**
     *
     * Tells the Server what to do when a Message of this type was received
     *
     * @param connection the Connection that the Message was received on
     * @throws UnsupportedSideException if the Message was received on the wrong side
     */
    public abstract void handleOnServer(Connection connection) throws UnsupportedSideException;

}
