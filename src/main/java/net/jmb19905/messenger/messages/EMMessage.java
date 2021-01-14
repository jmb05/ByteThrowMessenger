package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;

public abstract class EMMessage {

    public abstract void handleOnClient(Connection connection) throws UnsupportedSideException;

    public abstract void handleOnServer(Connection connection) throws UnsupportedSideException;

}
