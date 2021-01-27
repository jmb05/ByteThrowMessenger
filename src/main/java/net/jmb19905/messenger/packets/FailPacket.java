package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;

import javax.swing.*;

public class FailPacket extends BTMPacket {

    public String type;
    public String cause;

    public FailPacket(){}

    @Override
    public void handleOnClient(Connection connection){
        switch (type) {
            case "loginFail":
                JOptionPane.showMessageDialog(ByteThrowClient.window, "Could not log in! " + (cause.equals("pw") ? "Password " : "Username ") + "was incorrect.", "Wrong credentials", JOptionPane.ERROR_MESSAGE);
                ByteThrowClient.wipeUserData();
                ByteThrowClient.messagingClient.login(connection);
                break;
            case "notRegistered":
                int jop = JOptionPane.showConfirmDialog(ByteThrowClient.window, "Login failed. If you have no account you have to register.\nDo you want to register?", "Login failed", JOptionPane.YES_NO_CANCEL_OPTION);
                if (jop == JOptionPane.YES_OPTION) {
                    ByteThrowClient.messagingClient.register(connection);
                } else if (jop == JOptionPane.NO_OPTION) {
                    ByteThrowClient.messagingClient.login(connection);
                } else {
                    ByteThrowClient.messagingClient.stop(0);
                }
                break;
            case "registerFail":
                JOptionPane.showMessageDialog(ByteThrowClient.window, "Server Error registering user. " + cause + ". Please again try later.", "Error registering", JOptionPane.ERROR_MESSAGE);
                ByteThrowClient.wipeUserData();
                ByteThrowClient.messagingClient.login(connection);
                break;
            case "usernameTaken":
                ByteThrowClient.setUserData("", "");
                ByteThrowClient.setLoggedIn(false);
                ByteThrowClient.messagingClient.register(connection);
                break;
            case "outOfDate":
                JOptionPane.showMessageDialog(ByteThrowClient.window, "Your Client is out of Date. The Server is on Version: " + cause + ". You are on Version: " + ByteThrowClient.version + ". Please install the newest version.", "Client Out Of Date", JOptionPane.ERROR_MESSAGE);
                Window.closeRequested = true;
                ByteThrowClient.messagingClient.stop(0);
                break;
            case "connectFail":
                JOptionPane.showMessageDialog(ByteThrowClient.window, "There was an error when trying to connect with" + cause, "ERROR", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("FailPacket received on server");
    }
}
