package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.ClientSerializationUtils;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;

import javax.swing.*;

public class FailPacket extends BTMPacket {

    public String type;
    public String cause;
    public String message;

    public FailPacket(){}

    @Override
    public void handleOnClient(Connection connection){
        switch (type) {
            case "loginFail":
                JOptionPane.showMessageDialog(ByteThrowClient.window, "Could not log in! " + (cause.equals("pw") ? "Password " : "Username ") + "was incorrect.", "Wrong credentials", JOptionPane.ERROR_MESSAGE);
                ByteThrowClient.messagingClient.login(connection, ByteThrowClient.getUserSession().username, "");
                ClientSerializationUtils.wipeUserData(ByteThrowClient.getUserSession());
                break;
            case "notRegistered":
                int jop = JOptionPane.showConfirmDialog(ByteThrowClient.window, "Login failed. If you have no account you have to register.\nDo you want to register?", "Login failed", JOptionPane.YES_NO_CANCEL_OPTION);
                if (jop == JOptionPane.YES_OPTION) {
                    ByteThrowClient.messagingClient.register(connection);
                } else if (jop == JOptionPane.NO_OPTION) {
                    ByteThrowClient.messagingClient.login(connection, "", "");
                } else {
                    ByteThrowClient.messagingClient.stop(0);
                }
                break;
            case "internal":
                JOptionPane.showMessageDialog(ByteThrowClient.window,  message + ". Please again try later.", "Server Error", JOptionPane.ERROR_MESSAGE);
                ByteThrowClient.messagingClient.login(connection, ByteThrowClient.getUserSession().username, ByteThrowClient.getUserSession().password);
                if(cause.equals("change")){
                    ByteThrowClient.setSessionLogIn(true);
                }else if(cause.equals("register")){
                    ClientSerializationUtils.wipeUserData(ByteThrowClient.getUserSession());
                    ByteThrowClient.setSessionLogIn(false);
                }
                break;
            case "usernameTaken":
                if(cause.equals("initial")) {
                    ByteThrowClient.setUserSession("", "");
                    ByteThrowClient.setSessionLogIn(false);
                    ByteThrowClient.messagingClient.register(connection);
                }else if(cause.equals("change")){
                    JOptionPane.showMessageDialog(ByteThrowClient.window, "Error changing username. Username is taken.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case "outOfDate":
                JOptionPane.showMessageDialog(ByteThrowClient.window, "Your Client is out of Date. The Server is on Version: " + cause + ". You are on Version: " + ByteThrowClient.version + ". Please install the newest version.", "Client Out Of Date", JOptionPane.ERROR_MESSAGE);
                Window.closeRequested = true;
                ByteThrowClient.messagingClient.stop(0);
                break;
            case "connectFail":
                JOptionPane.showMessageDialog(ByteThrowClient.window, "There was an error when trying to connect with" + cause, "ERROR", JOptionPane.ERROR_MESSAGE);
                break;
            case "changeName":
                JOptionPane.showMessageDialog(ByteThrowClient.window, "Cannot change name of other person!", "ERROR", JOptionPane.ERROR_MESSAGE);
                break; //This OptionPane should never be seen or something is really wrong with the client (e.g. modified)
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("FailPacket received on server");
    }
}
