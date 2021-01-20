package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.logging.BTMLogger;

public class SuccessPacket extends BTMPacket {

    public String type;

    @Override
    public void handleOnClient(Connection connection) {
        if(type.equals("login")){
            BTMLogger.info("MessagingClient", "Logged in successfully");
            ByteThrowClient.writeUserData();
            ByteThrowClient.setLoggedIn(true);
            MessagingClient.initOtherUsers();
        }else if(type.equals("register")){
            ByteThrowClient.writeUserData();
            ByteThrowClient.setLoggedIn(true);
            MessagingClient.initOtherUsers();
            BTMLogger.info("MessagingClient", "Registered Successful");
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("SuccessPacket received on server");
    }
}
