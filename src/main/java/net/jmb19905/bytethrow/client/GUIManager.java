/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.client;

import net.jmb19905.bytethrow.client.gui.LoginDialog;
import net.jmb19905.bytethrow.client.gui.RegisterDialog;
import net.jmb19905.bytethrow.client.gui.Window;
import net.jmb19905.bytethrow.client.gui.settings.AccountSettings;
import net.jmb19905.bytethrow.client.gui.settings.SettingsWindow;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicReference;

public class GUIManager {

    private final Window window;

    public GUIManager(){
        this.window = new Window();

        ShutdownManager.addCleanUp(() -> {
            window.setEnabled(false);
            window.dispose();
        });
    }

    public void setUsername(String username){
        window.getAccountSettings().setUsername(username);
    }

    public void addPeer(String name){
        window.addPeer(name);
    }

    public void removePeer(String name){
        window.removePeer(name);
    }

    public void setPeers(String[] names){
        window.setPeers(names);
    }

    public void setPeerStatus(String peer, boolean status){
        window.setPeerStatus(peer, status);
    }

    public void appendLine(String line){
        window.appendLine(line);
    }

    public void append(String text, AttributeSet attributeSet){
        window.append(text, attributeSet);
    }

    public void newLine(){
        window.newLine();
    }

    public void appendMessage(String sender, String message){
        window.appendMessage(sender, message);
    }

    public void showLocalisedError(String id){
        showError(Localisation.get(id), "");
    }

    public void showError(String message){
        JOptionPane.showMessageDialog(window, message, "", JOptionPane.ERROR_MESSAGE);
    }

    public void showError(String message, String title){
        JOptionPane.showMessageDialog(window, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void showLoading(boolean load){
        window.showLoading(load);
    }

    public void repaint(){
        window.repaint();
        SettingsWindow settingsWindow = window.getSettingsWindow();
        if(settingsWindow != null){
            settingsWindow.repaint();
        }
        AccountSettings accountSettings = window.getAccountSettings();
        if(accountSettings != null){
            accountSettings.repaint();
        }
    }

    public void pack(){
        window.pack();
        SettingsWindow settingsWindow = window.getSettingsWindow();
        if(settingsWindow != null){
            settingsWindow.pack();
        }
        AccountSettings accountSettings = window.getAccountSettings();
        if(accountSettings != null){
            accountSettings.pack();
        }
    }

    public void updateComponentTree(){
        SwingUtilities.updateComponentTreeUI(window);
        SettingsWindow settingsWindow = window.getSettingsWindow();
        if(settingsWindow != null){
            SwingUtilities.updateComponentTreeUI(settingsWindow);
        }
        AccountSettings accountSettings = window.getAccountSettings();
        if(accountSettings != null){
            SwingUtilities.updateComponentTreeUI(accountSettings);
        }
    }

    public LoginData showLoginDialog(ActionListener registerListener){
        Object lockObj = new Object();
        AtomicReference<String> username = new AtomicReference<>();
        AtomicReference<String> password = new AtomicReference<>();
        LoginDialog loginDialog = new LoginDialog("", "", "", true, window);
        loginDialog.addConfirmButtonActionListener(l -> {
            synchronized (lockObj) {
                username.set(loginDialog.getUsername());
                password.set(loginDialog.getPassword());
                lockObj.notifyAll();
            }
        });
        loginDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ShutdownManager.shutdown(0);
            }
        });
        loginDialog.addRegisterButtonActionListener(registerListener);
        loginDialog.setVisible(true);
        synchronized (lockObj){
            while (username.get() == null && password.get() == null){
                try {
                    lockObj.wait();
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        }
        return new LoginData(username.get(), password.get());
    }

    public LoginData showRegisterDialog(boolean securePasswords, ActionListener loginListener){
        Object lockObj = new Object();
        AtomicReference<String> username = new AtomicReference<>();
        AtomicReference<String> password = new AtomicReference<>();
        RegisterDialog registerDialog = new RegisterDialog(securePasswords, window);
        registerDialog.addConfirmButtonActionListener(l -> {
            synchronized (lockObj) {
                username.set(registerDialog.getUsername());
                password.set(registerDialog.getPassword());
                lockObj.notifyAll();
            }
        });
        registerDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ShutdownManager.shutdown(0);
            }
        });
        registerDialog.addLoginButtonActionListener(loginListener);
        registerDialog.showDialog();
        synchronized (lockObj){
            while (username.get() == null && password.get() == null){
                try {
                    lockObj.wait();
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        }
        return new LoginData(username.get(), password.get());
    }

    public static record LoginData(String username, String password){ }

}