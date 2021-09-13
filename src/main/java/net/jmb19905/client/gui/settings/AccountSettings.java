package net.jmb19905.client.gui.settings;

import net.jmb19905.client.ClientMain;
import net.jmb19905.client.StartClient;
import net.jmb19905.client.gui.ConfirmIdentityDialog;
import net.jmb19905.common.packets.ChangeUserDataPacket;
import net.jmb19905.common.packets.LoginPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.common.util.ResourceUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Interface that allows the user to change username, password, etc. of their account
 */
public class AccountSettings extends JDialog {

    private Icon userIcon;
    private final JLabel nameLabel;

    private final ConfirmIdentityDialog confirmIdentityDialog;

    public AccountSettings(Icon userIcon){
        super(StartClient.window);
        this.userIcon = userIcon;
        this.confirmIdentityDialog = new ConfirmIdentityDialog();
        setTitle("Account Settings");
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
            name = StartClient.client.name;
        }catch (NullPointerException ignored){}
        nameLabel = new JLabel(name);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        add(nameLabel, constraints);

        JButton changeUsernameButton = new JButton();
        Action changeUsernameAction = new AbstractAction("Change Username") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (isIdentityConfirmed()) {
                    String newUsername = JOptionPane.showInputDialog("New Username: ");
                    if (!newUsername.strip().equals("")) {
                        changeUsername(newUsername);
                    } else {
                        JOptionPane.showMessageDialog(null, "Nothing changed");
                    }
                } else {
                    confirmIdentityDialog.addConfirmButtonActionListener(ae -> sendConfirmIdentityPacket(confirmIdentityDialog.getUsername(), confirmIdentityDialog.getPassword()));
                    confirmIdentityDialog.addIdentityConfirmedActionListener(ae -> changeUsername(JOptionPane.showInputDialog("New Username: ")));
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

        JButton changePasswordButton = new JButton();
        Action changePasswordAction = new AbstractAction("Change Password") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(isIdentityConfirmed()){
                    String newPassword = JOptionPane.showInputDialog("New Password: ");
                    if(!newPassword.strip().equals("")) {
                        changePassword(newPassword);
                    }else {
                        JOptionPane.showMessageDialog(null, "Nothing changed");
                    }
                }else {
                    confirmIdentityDialog.addConfirmButtonActionListener(ae -> sendConfirmIdentityPacket(confirmIdentityDialog.getUsername(), confirmIdentityDialog.getPassword()));
                    confirmIdentityDialog.addIdentityConfirmedActionListener(ae -> changePassword(JOptionPane.showInputDialog("New Password: ")));
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

        JButton changeIconButton = new JButton("Change avatar");
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
            identityConfirmed = StartClient.client.isIdentityConfirmed();
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

    private void sendConfirmIdentityPacket(String username, String password){
        try {
            StartClient.client.name = username;
            StartClient.window.getAccountSettings().setUsername(StartClient.client.name);

            LoginPacket loginPacket = new LoginPacket(false);
            loginPacket.name = StartClient.client.name;
            loginPacket.password = password;
            loginPacket.confirmIdentity = true;

            NetworkingUtility.sendPacket(loginPacket, StartClient.client.getToServerChannel(), StartClient.client.getHandler().getEncryption());
        }catch (NullPointerException ignored){}
    }

    private void changeUsername(String username){
        ChangeUserDataPacket packet = new ChangeUserDataPacket();
        packet.type = "username";
        packet.value = username;

        NetworkingUtility.sendPacket(packet, StartClient.client.getToServerChannel(), StartClient.client.getHandler().getEncryption());
    }

    private void changePassword(String password){
        try {
            ChangeUserDataPacket packet = new ChangeUserDataPacket();
            packet.type = "password";
            packet.value = password;

            NetworkingUtility.sendPacket(packet, StartClient.client.getToServerChannel(), StartClient.client.getHandler().getEncryption());
        }catch (NullPointerException ignored){}
    }

    private void changeAvatar(){
        JOptionPane.showMessageDialog(this, "An avatar is currently not implemented!");
        Logger.log("Avatar isn't implemented yet!", Logger.Level.WARN);//TODO: implement an avatar
    }
}
