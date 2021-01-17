package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;

public interface IQueueable {

    /**
     *
     * Tells the server what to do when the recipient of a queued message connects
     *
     * @param connection the Connection that the Message will be sent on
     * @param extraData the extra information that the message needs to function
     */
    void handleOnQueue(Connection connection, Object[] extraData);

}
