package net.jmb19905.messenger.client.ui.settings;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.ClientUtils;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.client.ui.util.dialoges.LoginDialog;
import net.jmb19905.messenger.util.FileUtility;

public class AccountSettings extends JDialog {

    private Icon userIcon;
    private String username;

    public AccountSettings(Icon userIcon, String username){
        this.userIcon = userIcon;
        this.username = username;
        setTitle("Account Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(450, 300));
        setIconImage(FileUtility.getImageResource("icon.png"));
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.weightx = 1;
        constraints.weighty = 1;

        JLabel iconLabel = new JLabel(userIcon);
        iconLabel.setBorder(BorderFactory.createEtchedBorder());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        add(iconLabel, constraints);

        JLabel nameLabel = new JLabel(username);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        add(nameLabel, constraints);

        JButton changeIconButton = new JButton("Change avatar");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        add(changeIconButton, constraints);

        JButton changeUsernameButton = new JButton("Change Username");
        changeUsernameButton.addActionListener(e -> {
            LoginDialog loginDialog = new LoginDialog("", "", "Please verify your identity:");
            loginDialog.addConfirmButtonActionListener(ae -> {
                String newUsername = JOptionPane.showInputDialog(loginDialog, "New Username: ");
                ByteThrowClient.messagingClient.client.sendTCP(ClientUtils.createChangeUsernamePacket(newUsername));
                ByteThrowClient.setLoggedIn(false);
                loginDialog.dispose();
            });
            loginDialog.showDialog();
        });
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        add(changeUsernameButton, constraints);

        JButton changePasswordButton = new JButton("Change Password");
        changeUsernameButton.addActionListener(e -> {
            LoginDialog loginDialog = new LoginDialog("", "", "Please verify your identity:");
            loginDialog.addConfirmButtonActionListener(ae -> {
                String newPassword = JOptionPane.showInputDialog(loginDialog, "New Password: ");
                ByteThrowClient.messagingClient.client.sendTCP(ClientUtils.createChangePasswordPacket(newPassword));
                ByteThrowClient.setLoggedIn(false);
                loginDialog.dispose();
            });
            loginDialog.showDialog();
        });
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        add(changePasswordButton, constraints);

        pack();
    }

    public void showDialog(){
        pack();
        setVisible(true);
    }

    public Icon getUserIcon() {
        return userIcon;
    }

    public String getUsername() {
        return username;
    }

    public void setUserIcon(Icon userIcon) {
        this.userIcon = userIcon;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
