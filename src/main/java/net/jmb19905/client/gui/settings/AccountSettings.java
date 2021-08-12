package net.jmb19905.client.gui.settings;

import net.jmb19905.client.ClientMain;
import net.jmb19905.client.gui.ConfirmIdentityDialog;
import net.jmb19905.common.packets.ChangeUserDataPacket;
import net.jmb19905.common.util.ResourceUtility;
import net.jmb19905.common.packets.LoginPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;

import javax.swing.*;
import java.awt.*;

/**
 * Interface that allows the user to change username, password, etc. of their account
 */
public class AccountSettings extends JDialog {

    private Icon userIcon;
    private final JLabel nameLabel;

    private final ConfirmIdentityDialog confirmIdentityDialog;

    public AccountSettings(Icon userIcon){
        this.userIcon = userIcon;
        this.confirmIdentityDialog = new ConfirmIdentityDialog();
        setTitle("Account Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(450, 300));
        setIconImage(ResourceUtility.getImageResource("icons/icon.png"));
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.weightx = 1;
        constraints.weighty = 1;

        JLabel iconLabel = new JLabel(userIcon);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        add(iconLabel, constraints);

        nameLabel = new JLabel(ClientMain.client.name);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        add(nameLabel, constraints);

        JButton changeIconButton = new JButton("Change avatar");
        changeIconButton.addActionListener(l -> changeAvatar());
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        add(changeIconButton, constraints);

        JButton changeUsernameButton = new JButton("Change Username");
        changeUsernameButton.addActionListener(e -> {
            if(ClientMain.client.isIdentityConfirmed()){
                changeUsername(JOptionPane.showInputDialog("New Username: "));
            }else {
                confirmIdentityDialog.addConfirmButtonActionListener(ae -> sendConfirmIdentityPacket(confirmIdentityDialog.getUsername(), confirmIdentityDialog.getPassword()));
                confirmIdentityDialog.addIdentityConfirmedActionListener(ae -> changeUsername(JOptionPane.showInputDialog("New Username: ")));
                confirmIdentityDialog.setVisible(true);
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        add(changeUsernameButton, constraints);

        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> {
            if(ClientMain.client.isIdentityConfirmed()){
                changePassword(JOptionPane.showInputDialog("New Password: "));
            }else {
                confirmIdentityDialog.addConfirmButtonActionListener(ae -> sendConfirmIdentityPacket(confirmIdentityDialog.getUsername(), confirmIdentityDialog.getPassword()));
                confirmIdentityDialog.addIdentityConfirmedActionListener(ae -> changePassword(JOptionPane.showInputDialog("New Password: ")));
                confirmIdentityDialog.setVisible(true);
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        add(changePasswordButton, constraints);

        pack();
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
        ClientMain.client.name = username;
        ClientMain.window.getAccountSettings().setUsername(ClientMain.client.name);

        LoginPacket loginPacket = new LoginPacket(false);
        loginPacket.name = ClientMain.client.name;
        loginPacket.password = password;
        loginPacket.confirmIdentity = true;

        NetworkingUtility.sendPacket(loginPacket, ClientMain.client.getToServerChannel(), ClientMain.client.getHandler().getEncryption());
    }

    private void changeUsername(String username){
        ChangeUserDataPacket packet = new ChangeUserDataPacket();
        packet.type = "username";
        packet.value = username;

        NetworkingUtility.sendPacket(packet, ClientMain.client.getToServerChannel(), ClientMain.client.getHandler().getEncryption());
    }

    private void changePassword(String password){
        ChangeUserDataPacket packet = new ChangeUserDataPacket();
        packet.type = "password";
        packet.value = password;

        NetworkingUtility.sendPacket(packet, ClientMain.client.getToServerChannel(), ClientMain.client.getHandler().getEncryption());
    }

    private void changeAvatar(){
        JOptionPane.showMessageDialog(this, "An avatar is currently not implemented!");
        Logger.log("Avatar isn't implemented yet!", Logger.Level.WARN);//TODO: implement an avatar
    }
}
