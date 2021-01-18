package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.EMLogger;

public class SuccessPackage extends EMPackage {

    public String type;

    @Override
    public void handleOnClient(Connection connection) {
        if(type.equals("login")){
            EMLogger.info("MessagingClient", "Logged in successfully");
            EncryptedMessenger.writeUserData();
            EncryptedMessenger.setLoggedIn(true);
            MessagingClient.initOtherUsers();
        }else if(type.equals("register")){
            EncryptedMessenger.writeUserData();
            EncryptedMessenger.setLoggedIn(true);
            MessagingClient.initOtherUsers();
            EMLogger.info("MessagingClient", "Registered Successful");
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("SuccessPackage received on server");
    }
}
