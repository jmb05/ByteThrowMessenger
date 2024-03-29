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

package net.jmb19905.bytethrow.client.gui.settings;

import net.jmb19905.bytethrow.client.ClientManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.gui.ConfirmIdentityDialog;
import net.jmb19905.bytethrow.client.gui.Window;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.packets.ChangeUserDataPacket;
import net.jmb19905.bytethrow.common.packets.LoginPacket;
import net.jmb19905.bytethrow.common.util.ResourceUtility;
import net.jmb19905.util.Localisation;
import net.jmb19905.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Interface that allows the user to change username, password, etc. of their account
 */
public class AccountSettings extends JDialog {

    private Icon userIcon;
    private final JLabel nameLabel;
    private final JButton changeUsernameButton;
    private final JButton changePasswordButton;
    private final JButton changeIconButton;

    private final ConfirmIdentityDialog confirmIdentityDialog;

    private final ClientManager manager;

    public AccountSettings(Icon userIcon, Window window) {
        super(window);
        this.manager = StartClient.manager;

        this.userIcon = userIcon;
        this.confirmIdentityDialog = new ConfirmIdentityDialog(window);
        setTitle(Localisation.get("account_settings"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //setPreferredSize(new Dimension(450, 300));
        setIconImage(ResourceUtility.getImageResource("icons/icon.png"));
        setLayout(new GridBagLayout());
        setModal(true);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.ipadx = 10;
        constraints.ipady = 10;
        constraints.weightx = 1;
        constraints.weighty = 1;

        JLabel iconLabel = new JLabel(userIcon);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        add(iconLabel, constraints);

        String name = "";
        try {
            name = manager.user.getUsername();
        } catch (NullPointerException ignored) {
        }
        nameLabel = new JLabel(name);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        add(nameLabel, constraints);

        changeUsernameButton = new JButton(Localisation.get("change_username"));
        Action changeUsernameAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (isIdentityConfirmed()) {
                    String newUsername = JOptionPane.showInputDialog(Localisation.get("new_username"));
                    if (!newUsername.strip().equals("")) {
                        changeUsername(newUsername);
                    } else {
                        JOptionPane.showMessageDialog(null, Localisation.get("nothing_changed"));
                    }
                } else {
                    confirmIdentityDialog.addConfirmButtonActionListener(ae -> sendConfirmIdentityPacket(new User(confirmIdentityDialog.getUsername(), confirmIdentityDialog.getPassword())));
                    confirmIdentityDialog.addIdentityConfirmedActionListener(ae -> changeUsername(JOptionPane.showInputDialog(Localisation.get("new_username"))));
                    confirmIdentityDialog.setVisible(true);
                    changeUsernameButton.getAction().actionPerformed(actionEvent);
                }
            }
        };
        changeUsernameButton.setAction(changeUsernameAction);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        add(changeUsernameButton, constraints);

        changePasswordButton = new JButton(Localisation.get("change_password"));
        Action changePasswordAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (isIdentityConfirmed()) {
                    String newPassword = JOptionPane.showInputDialog(Localisation.get("new_password"));
                    if (!newPassword.strip().equals("")) {
                        changePassword(newPassword);
                    } else {
                        JOptionPane.showMessageDialog(null, Localisation.get("nothing_changed"));
                    }
                } else {
                    confirmIdentityDialog.addConfirmButtonActionListener(ae -> sendConfirmIdentityPacket(new User(confirmIdentityDialog.getUsername(), confirmIdentityDialog.getPassword())));
                    confirmIdentityDialog.addIdentityConfirmedActionListener(ae -> changePassword(JOptionPane.showInputDialog(Localisation.get("new_password"))));
                    confirmIdentityDialog.setVisible(true);
                    changePasswordButton.getAction().actionPerformed(actionEvent);
                }
            }
        };
        changePasswordButton.setAction(changePasswordAction);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        add(changePasswordButton, constraints);

        changeIconButton = new JButton(Localisation.get("change_avatar"));
        changeIconButton.addActionListener(l -> changeAvatar());
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        add(changeIconButton, constraints);

        pack();
        setLocationRelativeTo(null);
    }

    private boolean isIdentityConfirmed() {
        boolean identityConfirmed = false;
        try {
            identityConfirmed = manager.isIdentityConfirmed();
        } catch (NullPointerException ignored) {
        }
        return identityConfirmed;
    }

    public Icon getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(Icon userIcon) {
        this.userIcon = userIcon;
    }

    public void setUsername(String username) {
        this.nameLabel.setText(username);
    }

    private void sendConfirmIdentityPacket(User user) {
        try {
            manager.user.setUsername(user.getUsername());
            setUsername(user.getUsername());

            LoginPacket loginPacket = new LoginPacket();
            loginPacket.user = user;
            loginPacket.confirmIdentity = true;

            manager.send(loginPacket);
        } catch (NullPointerException ignored) {
        }
    }

    private void changeUsername(String username) {
        ChangeUserDataPacket packet = new ChangeUserDataPacket();
        packet.type = "username";
        packet.value = username;
        manager.send(packet);
    }

    private void changePassword(String password) {
        try {
            ChangeUserDataPacket packet = new ChangeUserDataPacket();
            packet.type = "password";
            packet.value = password;
            manager.send(packet);
        } catch (NullPointerException ignored) {
        }
    }

    private void changeAvatar() {
        JOptionPane.showMessageDialog(this, Localisation.get("avatar_unimplemented"));
        Logger.warn(Localisation.get("avatar_unimplemented"));//TODO: implement an avatar
    }

    public void reloadLang() {
        changeUsernameButton.setText(Localisation.get("change_username"));
        changePasswordButton.setText(Localisation.get("change_password"));
        changeIconButton.setText(Localisation.get("change_avatar"));
        setTitle(Localisation.get("account_settings"));
        pack();
    }
}
