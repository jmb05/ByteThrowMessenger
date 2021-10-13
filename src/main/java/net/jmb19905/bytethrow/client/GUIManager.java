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

package net.jmb19905.bytethrow.client;

import net.jmb19905.bytethrow.client.gui.LoginDialog;
import net.jmb19905.bytethrow.client.gui.RegisterDialog;
import net.jmb19905.bytethrow.client.gui.Window;
import net.jmb19905.bytethrow.client.gui.settings.AccountSettings;
import net.jmb19905.bytethrow.client.gui.settings.SettingsWindow;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.util.ShutdownManager;

import javax.swing.*;
import javax.swing.text.AttributeSet;

public class GUIManager {

    private final Window window;
    private final LoginDialog loginDialog;
    private final RegisterDialog registerDialog;

    public GUIManager(){
        this.window = new Window();
        loginDialog = new LoginDialog("", "", "", true, window);
        registerDialog = new RegisterDialog(true, window);

        ShutdownManager.addCleanUp(() -> SwingUtilities.invokeLater(() -> {
            window.setEnabled(false);
            window.dispose();
            loginDialog.dispose();
            registerDialog.dispose();
        }));
    }

    public void setUsername(String username){
        SwingUtilities.invokeLater(() -> window.getAccountSettings().setUsername(username));
    }

    public void addPeer(String name){
        SwingUtilities.invokeLater(() -> window.addPeer(name));
    }

    public void removePeer(String name){
        SwingUtilities.invokeLater(() -> window.removePeer(name));
    }

    public void setPeers(String[] names){
        SwingUtilities.invokeLater(() -> window.setPeers(names));
    }

    public void setPeerStatus(String peer, boolean status){
        SwingUtilities.invokeLater(() -> window.setPeerStatus(peer, status));
    }

    public void appendLine(String line){
        SwingUtilities.invokeLater(() -> window.appendLine(line));
    }

    public void append(String text, AttributeSet attributeSet){
        SwingUtilities.invokeLater(() -> window.append(text, attributeSet));
    }

    public void newLine(){
        SwingUtilities.invokeLater(window::newLine);
    }

    public void appendMessage(String sender, String message){
        SwingUtilities.invokeLater(() -> window.appendMessage(sender, message));
    }

    public void showLocalisedError(String id){
        SwingUtilities.invokeLater(() -> showError(Localisation.get(id), ""));
    }

    public void showError(String message){
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(window, message, "", JOptionPane.ERROR_MESSAGE));
    }

    public void showError(String message, String title){
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(window, message, title, JOptionPane.ERROR_MESSAGE));
    }

    public void showLoading(boolean load){
        SwingUtilities.invokeLater(() -> window.showLoading(load));
    }

    public void repaint(){
        SwingUtilities.invokeLater(() -> {
            window.repaint();
            SettingsWindow settingsWindow = window.getSettingsWindow();
            if(settingsWindow != null){
                settingsWindow.repaint();
            }
            AccountSettings accountSettings = window.getAccountSettings();
            if(accountSettings != null){
                accountSettings.repaint();
            }
        });
    }

    public void pack(){
        SwingUtilities.invokeLater(() -> {
            window.pack();
            SettingsWindow settingsWindow = window.getSettingsWindow();
            if (settingsWindow != null) {
                settingsWindow.pack();
            }
            AccountSettings accountSettings = window.getAccountSettings();
            if (accountSettings != null) {
                accountSettings.pack();
            }
        });
    }

    public void updateComponentTree(){
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(window);
            SettingsWindow settingsWindow = window.getSettingsWindow();
            if (settingsWindow != null) {
                SwingUtilities.updateComponentTreeUI(settingsWindow);
            }
            AccountSettings accountSettings = window.getAccountSettings();
            if (accountSettings != null) {
                SwingUtilities.updateComponentTreeUI(accountSettings);
            }
        });
    }

    public LoginDialog.LoginData showLoginDialog(Runnable register){
        LoginDialog.LoginDataResult result = loginDialog.showDialog();
        if(result.resultType() == ResultType.CONFIRM){
            return result.loginData();
        }else if(result.resultType() == ResultType.CANCEL){
            ShutdownManager.shutdown(0);
        }else {
            register.run();
        }
        return null;
    }

    public RegisterDialog.RegisterData showRegisterDialog(Runnable login){
        RegisterDialog.RegisterDataResult result = registerDialog.showDialog();
        if(result.resultType() == ResultType.CONFIRM){
            return result.registerData();
        }else if(result.resultType() == ResultType.CANCEL){
            ShutdownManager.shutdown(0);
        }else {
            login.run();
        }
        return null;
    }



    public enum ResultType {CONFIRM, OTHER, CANCEL}

}