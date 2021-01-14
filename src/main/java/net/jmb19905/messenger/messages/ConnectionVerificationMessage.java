package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;

public class ConnectionVerificationMessage extends EMMessage{

    public String username;
    public String message;

    public static final String defaultMessage = "20210114-9:19";

    public ConnectionVerificationMessage(){}

    @Override
    public void handleOnClient(Connection connection) {

    }

    @Override
    public void handleOnServer(Connection connection) {

    }
}
