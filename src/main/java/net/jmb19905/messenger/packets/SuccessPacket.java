package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.swing.*;

public class SuccessPacket extends BTMPacket {

    public String type;

    public SuccessPacket(){}

    @Override
    public void handleOnClient(Connection connection) {
        if(type.equals("login")){
            BTMLogger.info("MessagingClient", "Logged in successfully");
            ByteThrowClient.writeUserData();
            ByteThrowClient.setLoggedIn(true);
            MessagingClient.initOtherUsers();
            JOptionPane.showMessageDialog(ByteThrowClient.window, "Login succeeded", "Success", JOptionPane.PLAIN_MESSAGE);
        }else if(type.equals("register")){
            ByteThrowClient.writeUserData();
            ByteThrowClient.setLoggedIn(true);
            MessagingClient.initOtherUsers();
            BTMLogger.info("MessagingClient", "Registered Successful");
            JOptionPane.showMessageDialog(ByteThrowClient.window, "Register succeeded", "Success", JOptionPane.PLAIN_MESSAGE);
        }
        ByteThrowClient.messagingClient.setKeepAliveRequired(false);
        try {
            ByteThrowClient.messagingClient.keepAlive.join();
        } catch (InterruptedException e) {
            BTMLogger.warn("MessagingClient", "KeepAlive Thread threw InterruptedException", e);
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("SuccessPacket received on server");
    }
}
