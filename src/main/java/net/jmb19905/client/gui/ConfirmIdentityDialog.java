package net.jmb19905.client.gui;

import net.jmb19905.client.networking.Client;
import net.jmb19905.client.util.Localisation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Shows a Login Dialog
 */
public class ConfirmIdentityDialog extends LoginDialog{

    protected ActionListener identityConfirmedListener = null;

    public ConfirmIdentityDialog() {
        super("", "", Localisation.get("confirm_id"), false);
    }

    @Override
    protected void confirmActionPerformed(ActionEvent e) {
        //Copied from Superclass
        super.username = super.usernameInputField.getText();
        super.password = new String(super.passwordInputField.getPassword());
        if(ConfirmIdentityDialog.super.confirmListener != null) {
            ConfirmIdentityDialog.super.confirmListener.actionPerformed(e);
        }

        long approximateStartTime = System.currentTimeMillis();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(Client.confirmIdentityPacket != null){
                    dispose();
                    if(identityConfirmedListener != null) {
                        identityConfirmedListener.actionPerformed(e);
                    }
                    Client.confirmIdentityPacket = null;
                    timer.cancel();
                }else if(System.currentTimeMillis() - approximateStartTime >= 30000){ //Timeout at 30 seconds
                    JOptionPane.showMessageDialog(null, Localisation.get("confirm_id_error"), "", JOptionPane.ERROR_MESSAGE);
                    timer.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(task, new Date(), 100);
        clearTextFields();
    }

    public void addIdentityConfirmedActionListener(ActionListener listener){
        identityConfirmedListener = listener;
    }

}
