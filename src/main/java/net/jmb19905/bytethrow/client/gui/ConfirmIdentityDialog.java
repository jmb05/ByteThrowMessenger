/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.client.gui;

import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.util.Localisation;

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

    public ConfirmIdentityDialog(Window window) {
        super("", "", Localisation.get("confirm_id"), false, window);
    }

    @Override
    protected void confirmActionPerformed(ActionEvent e) {
        //Copied from Superclass
        super.username = super.usernameInputField.getText();
        super.password = new String(super.passwordInputField.getPassword());
        if(ConfirmIdentityDialog.super.confirmListener != null) {
            ConfirmIdentityDialog.super.confirmListener.actionPerformed(e);
        }

        ClientManager manager = StartClient.manager;

        long approximateStartTime = System.currentTimeMillis();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(manager.confirmIdentityPacket != null){
                    dispose();
                    if(identityConfirmedListener != null) {
                        identityConfirmedListener.actionPerformed(e);
                    }
                    manager.confirmIdentityPacket = null;
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
