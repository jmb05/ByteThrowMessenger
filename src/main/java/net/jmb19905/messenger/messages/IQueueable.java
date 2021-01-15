package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;

public interface IQueueable {

    void handleOnQueue(Connection connection, Object[] extraData);

}
