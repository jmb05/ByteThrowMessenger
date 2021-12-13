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

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.gui.CreateGroupDialog;
import net.jmb19905.bytethrow.client.gui.LoginDialog;
import net.jmb19905.bytethrow.client.gui.RegisterDialog;
import net.jmb19905.bytethrow.client.gui.Window;
import net.jmb19905.bytethrow.client.gui.chatprofiles.*;
import net.jmb19905.bytethrow.client.gui.event.*;
import net.jmb19905.bytethrow.client.gui.settings.AccountSettings;
import net.jmb19905.bytethrow.client.gui.settings.SettingsWindow;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.bytethrow.common.chat.Message;
import net.jmb19905.bytethrow.common.chat.client.ClientGroupChat;
import net.jmb19905.bytethrow.common.chat.client.ClientPeerChat;
import net.jmb19905.bytethrow.common.chat.client.IClientChat;
import net.jmb19905.util.ShutdownManager;
import net.jmb19905.util.events.EventHandler;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Arrays;
import java.util.Enumeration;

public class GUIManager {

    private final Window window;
    private final LoginDialog loginDialog;
    private final RegisterDialog registerDialog;
    private final CreateGroupDialog createGroupDialog;

    private final EventHandler<GuiEventContext> handler;

    public GUIManager() {
        this.handler = new EventHandler<>("gui");
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, new FontUIResource("Noto Sans", Font.PLAIN, 14));
            }
        }

        this.window = new Window(handler);
        loginDialog = new LoginDialog(true, window);
        registerDialog = new RegisterDialog(true, window);
        createGroupDialog = new CreateGroupDialog(window);

        ShutdownManager.addCleanUp(() -> SwingUtilities.invokeLater(() -> {
            handler.setValid(false);
            loginDialog.dispose();
            registerDialog.dispose();
            createGroupDialog.dispose();
            window.setEnabled(false);
            window.dispose();
        }));
        handler.setValid(true);
    }

    public void addLoginEventListener(LoginEventListener listener) {
        handler.addEventListener(listener);
    }

    public void addRegisterEventListener(RegisterEventListener listener) {
        handler.addEventListener(listener);
    }

    public void addSendPeerMessageEventListener(SendPeerMessageEventListener listener) {
        handler.addEventListener(listener);
    }

    public void addSendGroupMessageEventListener(SendGroupMessageEventListener listener) {
        handler.addEventListener(listener);
    }

    public void setUsername(String username) {
        SwingUtilities.invokeLater(() -> window.getAccountSettings().setUsername(username));
    }

    public void addPeer(ClientPeerChat peerChat) {
        PeerChatProfile profile = new PeerChatProfile(peerChat);
        ProfilesManager.addProfile(profile);
        SwingUtilities.invokeLater(() -> window.addChat(profile));
    }

    public void removeChat(IClientChat<? extends Message> chat) {
        IChatProfile profile = ProfilesManager.getProfileByID(chat.getUniqueId());
        ProfilesManager.removeProfile(profile);
        SwingUtilities.invokeLater(() -> window.removeChat(profile));
    }

    public void setPeers(ClientPeerChat[] peers) {
        ProfilesManager.clear();
        Arrays.stream(peers).forEach(p -> ProfilesManager.addProfile(new PeerChatProfile(p)));
    }

    public void setPeerStatus(ClientPeerChat peerChat, boolean status) {
        SwingUtilities.invokeLater(() -> window.setPeerStatus((PeerChatProfile) ProfilesManager.getProfileByID(peerChat.getUniqueId()), status));
    }

    public void addGroup(ClientGroupChat groupChat) {
        GroupChatProfile profile = new GroupChatProfile(groupChat);
        ProfilesManager.addProfile(profile);
        SwingUtilities.invokeLater(() -> window.addChat(profile));
    }

    public <M extends Message> void appendMessage(M message, IClientChat<M> clientChat, boolean clearField){
        appendMessage(message, ((AbstractChatProfile) ProfilesManager.getProfileByID(clientChat.getUniqueId())), clearField);
    }

    public <M extends Message> void appendMessage(M message, AbstractChatProfile profile, boolean clearField){
        SwingUtilities.invokeLater(() -> window.appendMessage(message, profile, clearField));
    }

    public void showLocalisedError(String id) {
        showError(Localisation.get(id), "");
    }

    public void showError(String message) {
        showError(message, "");
    }

    public void showError(String message, String title) {
        JOptionPane.showMessageDialog(window, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void showLoading(boolean load) {
        SwingUtilities.invokeLater(() -> window.showLoading(load));
    }

    public void repaint() {
        SwingUtilities.invokeLater(() -> {
            window.repaint();
            SettingsWindow settingsWindow = window.getSettingsWindow();
            if (settingsWindow != null) {
                settingsWindow.repaint();
            }
            AccountSettings accountSettings = window.getAccountSettings();
            if (accountSettings != null) {
                accountSettings.repaint();
            }
        });
    }

    public void pack() {
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

    public void updateComponentTree() {
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

    public void showLoginDialog(ChannelHandlerContext ctx) {
        loginDialog.showDialog(handler, ctx);
    }

    public void showRegisterDialog(ChannelHandlerContext ctx) {
        registerDialog.showDialog(handler, ctx);
    }

    public CreateGroupDialog.CreateGroupData showCreateGroup() {
        CreateGroupDialog.CreateGroupData result = createGroupDialog.showDialog();
        if (!result.cancel()) {
            return result;
        }
        return null;
    }

    public enum ResultType {CONFIRM, OTHER, CANCEL}

}