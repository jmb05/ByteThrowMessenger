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

import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.gui.chatprofiles.*;
import net.jmb19905.bytethrow.client.gui.components.PicturePanel;
import net.jmb19905.bytethrow.client.gui.settings.AccountSettings;
import net.jmb19905.bytethrow.client.gui.settings.SettingsWindow;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.bytethrow.client.util.ThemeManager;
import net.jmb19905.bytethrow.common.chat.GroupMessage;
import net.jmb19905.bytethrow.common.chat.Message;
import net.jmb19905.bytethrow.common.chat.PeerMessage;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.ResourceUtility;
import net.jmb19905.bytethrow.common.util.Util;
import net.jmb19905.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The Window the Client sees
 */
public class Window extends JFrame {

    private final PeerList list;

    private final MessagesPane area;
    private final JTextField field;
    private final PicturePanel loadingPanel;
    private final JToolBar toolbar;
    private final JPanel peerPanel;
    private final JButton addPeer;

    private final SettingsWindow settingsWindow;
    private final AccountSettings accountSettings;

    /**
     * Initializes the components
     */
    public Window() {
        this.area = new MessagesPane();
        this.field = new JTextField();
        this.loadingPanel = new PicturePanel(new ImageIcon(ResourceUtility.getResourceAsURL("icons/spinner.gif")));
        setGlassPane(loadingPanel);
        setIconImage(ResourceUtility.getImageResource("icons/icon.png"));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(750, 500));

        JPanel messagingPanel = new JPanel(new GridBagLayout());
        peerPanel = new JPanel(new GridBagLayout());

        GridBagConstraints peerPanelConstraints = new GridBagConstraints();

        list = new PeerList();
        list.addListSelectionListener(l -> updateAreaContent(getSelected()));
        peerPanelConstraints.fill = GridBagConstraints.BOTH;
        peerPanelConstraints.weightx = 1;
        peerPanelConstraints.weighty = 1;
        peerPanel.add(list, peerPanelConstraints);

        addPeer = new JButton(Localisation.get("add_peer"));
        addPeer.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(Localisation.get("peer_name_input"));
            if (StartClient.manager != null && name != null && !name.equals("")) {
                StartClient.manager.connectToPeer(name);
            }
        });
        peerPanelConstraints.gridy = 1;
        peerPanelConstraints.weightx = 0;
        peerPanelConstraints.weighty = 0;
        peerPanelConstraints.insets = new Insets(5, 0, 0, 0);
        peerPanel.add(addPeer, peerPanelConstraints);

        JButton createGroup = new JButton(Localisation.get("create_group"));
        createGroup.addActionListener(l -> StartClient.manager.createGroup());
        peerPanelConstraints.gridy = 2;
        peerPanelConstraints.weightx = 0;
        peerPanelConstraints.weighty = 0;
        peerPanelConstraints.insets = new Insets(0, 0, 5, 0);
        peerPanel.add(createGroup, peerPanelConstraints);

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, peerPanel, messagingPanel);
        pane.setDividerSize(0);
        pane.setDividerLocation(this.getPreferredSize().width / 4);
        add(pane);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        messagingPanel.add(area, constraints);

        constraints.gridy = 1;
        constraints.weighty = 0;
        constraints.insets = new Insets(5, 0, 5, 0);
        messagingPanel.add(field, constraints);

        field.addActionListener(l -> send());

        toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolbar.setFloatable(false);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.weightx = 0;
        constraints.weighty = 1;
        constraints.insets = new Insets(0, 0, 0, 0);
        messagingPanel.add(toolbar, constraints);

        initToolBar();

        setTitle("ByteThrow Messenger - " + StartClient.version);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ConfigManager.saveClientConfig();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pane.setDividerLocation(getSize().width / 4);
            }
        });

        this.settingsWindow = new SettingsWindow();

        BufferedImage bufferedImage = ResourceUtility.getImageResource("icons/placeholder.png");
        ImageIcon icon = new ImageIcon(Util.cropImageToCircle(Util.toBufferedImage(bufferedImage.getScaledInstance(128, 128, 0))));
        this.accountSettings = new AccountSettings(icon, this);
        repaint();
    }

    private void reloadLang() {
        addPeer.setText(Localisation.get("add_peer"));
        settingsWindow.reloadLang();
    }

    public void showLoading(boolean loading) {
        loadingPanel.setVisible(loading);
    }

    public void setPeerStatus(PeerChatProfile profile, boolean status){
        list.setPeerStatus(profile, status);
    }

    /**
     * @return the current selected peer name
     */
    public IChatProfile<? extends Message> getSelected() {
        return list.getSelectedValue();
    }

    public boolean isSelectedGroup() {
        return list.getSelectedValue() instanceof GroupChatProfile;
    }

    public SettingsWindow getSettingsWindow() {
        return settingsWindow;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void repaint() {
        toolbar.setBackground(list.getBackground());
        peerPanel.setBackground(list.getBackground());
        reloadLang();
        initActions();
        initToolBar();
    }

    private void initToolBar() {
        toolbar.removeAll();
        toolbar.add(settingsAction);
        toolbar.add(Box.createVerticalGlue());
        toolbar.add(sendAction);
    }

    private Action settingsAction;

    private Action sendAction;

    private void initActions() {
        settingsAction = new AbstractAction("", ThemeManager.getIcon("settings_wheel")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu settingsMenu = new JPopupMenu("Settings");
                JMenuItem settingsMenuItem = new JMenuItem("General Settings");
                settingsMenuItem.addActionListener(l -> settingsWindow.setVisible(true));
                settingsMenu.add(settingsMenuItem);

                JMenuItem accountSettingsMenuItem = new JMenuItem("Account Settings");
                accountSettingsMenuItem.addActionListener(l -> accountSettings.setVisible(true));
                settingsMenu.add(accountSettingsMenuItem);

                settingsMenu.show(toolbar.getComponentAtIndex(0), toolbar.getComponentAtIndex(0).getX(), toolbar.getComponentAtIndex(0).getY());
            }
        };
        sendAction = new AbstractAction("", ThemeManager.getIcon("send")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                send();
            }
        };
    }

    public void addChat(IChatProfile<? extends Message> profile){
        list.addChat(profile);
    }

    public void removeChat(IChatProfile<? extends Message> profile){
        list.removeChat(profile);
    }

    private void send() {
        String text = field.getText();
        if (list.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(this, Localisation.get("select_peer"));
            return;
        }
        if (isSelectedGroup()) {
            sendToGroup(text);
        } else {
            sendToPeer(text);
        }
    }

    private void sendToGroup(String text) {
        GroupChatProfile chatProfile = (GroupChatProfile) getSelected();
        GroupMessage groupMessage = new GroupMessage(StartClient.manager.name, chatProfile.getDisplayName(), text, System.currentTimeMillis());
        if (StartClient.manager.sendGroupMessage(groupMessage)) {
            appendMessage(groupMessage, chatProfile);
            field.setText("");
        } else {
            JOptionPane.showMessageDialog(null, Localisation.get("chat_doesnt_exist"));
        }
    }

    private void sendToPeer(String text) {
        PeerChatProfile chatProfile = (PeerChatProfile) getSelected();
        PeerMessage peerMessage = new PeerMessage(StartClient.manager.name, chatProfile.getDisplayName(), text, System.currentTimeMillis());
        if (StartClient.manager.sendPeerMessage(peerMessage)) {
            appendMessage(peerMessage, chatProfile);
            field.setText("");
        } else {
            JOptionPane.showMessageDialog(null, Localisation.get("chat_doesnt_exist"));
        }
    }

    public <M extends Message> void appendMessage(M message, AbstractChatProfile<M> profile){
        Logger.debug("Trying to send Message (Window::appendMessage): " + getSelected());
        profile.addMessage(message);
        updateAreaContent(profile);
    }

    @SuppressWarnings("unchecked")
    private <M extends Message> void updateAreaContent(IChatProfile<M> profile){
        if(getSelected() == null) {
            area.clear();
            return;
        }
        if(getSelected().equals(profile)) {
            List<Message> messages = (List<Message>) profile.getMessages();
            area.setContent(messages);
        }
    }
}
