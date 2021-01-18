package net.jmb19905.messenger.packages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;

import javax.swing.*;

public class FailPackage extends EMPackage {

    public String type;
    public String cause;

    @Override
    public void handleOnClient(Connection connection){
        switch (type) {
            case "loginFail":
                JOptionPane.showMessageDialog(null, "Could not log in! " + (cause.equals("pw") ? "Password " : "Username ") + "was incorrect.", "Wrong credentials", JOptionPane.ERROR_MESSAGE);
                EncryptedMessenger.wipeUserData();
                EncryptedMessenger.messagingClient.login();
                break;
            case "notRegistered":
                int jop = JOptionPane.showConfirmDialog(null, "Login failed. If you have no account you have to register.\nDo you want to register?", "Login failed", JOptionPane.YES_NO_CANCEL_OPTION);
                if (jop == JOptionPane.YES_OPTION) {
                    EncryptedMessenger.messagingClient.register();
                } else if (jop == JOptionPane.NO_OPTION) {
                    EncryptedMessenger.messagingClient.login();
                } else {
                    EncryptedMessenger.messagingClient.stop(0);
                }
                break;
            case "registerFail":
                JOptionPane.showMessageDialog(null, "Server Error registering user. " + cause + ". Please again try later.", "Error registering", JOptionPane.ERROR_MESSAGE);
                EncryptedMessenger.wipeUserData();
                EncryptedMessenger.messagingClient.login();
                break;
            case "usernameTaken":
                EncryptedMessenger.setUserData("", "");
                EncryptedMessenger.setLoggedIn(false);
                EncryptedMessenger.messagingClient.register();
                break;
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("FailPackage received on server");
    }
}
