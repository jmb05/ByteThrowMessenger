package net.jmb19905.client.gui;

import net.jmb19905.client.gui.components.HintPasswordField;
import net.jmb19905.client.gui.components.HintTextField;
import net.jmb19905.common.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class RegisterDialog extends JDialog {
    private ActionListener confirmListener = e -> {};
    private ActionListener loginListener = e -> {};
    private WindowAdapter cancelListener = new WindowAdapter() {};

    private String username = "";
    private String password = "";
    private final JTextField usernameInputField;
    private JPasswordField passwordInputField1 = null;
    private JPasswordField passwordInputField2 = null;

    public RegisterDialog(boolean useStandardPasswordRules){
        setModal(true);
        setResizable(false);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(250, 275));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Register");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                username = "";
                password = "";
                dispose();
                cancelListener.windowClosing(e);
            }
        });

        Action confirmAction = new AbstractAction("Confirm") {
            @Override
            public void actionPerformed(ActionEvent e) {
                username = "";
                password = "";
                String newPassword = new String(passwordInputField1.getPassword());
                if(!Arrays.equals(passwordInputField1.getPassword(), passwordInputField2.getPassword())){
                    showPasswordsDoNotMatchPane();
                }else if(!useStandardPasswordRules || Util.checkPasswordRules(newPassword)){
                    username = usernameInputField.getText();
                    password = newPassword;
                    dispose();
                    confirmListener.actionPerformed(e);
                }else{
                    showPasswordCriteriaNotMetPane();
                }
            }
        };

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 15, 5, 15);

        usernameInputField = new HintTextField("Username");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        usernameInputField.addActionListener(l -> {
            if(passwordInputField1.getPassword().length == 0){
                passwordInputField1.requestFocus();
            }else if(passwordInputField2.getPassword().length == 0){
                passwordInputField2.requestFocus();
            }else {
                confirmAction.actionPerformed(l);
            }
        });
        add(usernameInputField, constraints);

        passwordInputField1 = new HintPasswordField("Password");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        passwordInputField1.addActionListener(l -> {
            if(passwordInputField2.getPassword().length == 0){
                passwordInputField2.requestFocus();
            }else {
                confirmAction.actionPerformed(l);
            }
        });
        add(passwordInputField1, constraints);

        passwordInputField2 = new HintPasswordField("Repeat Password");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        passwordInputField2.addActionListener(confirmAction);
        add(passwordInputField2, constraints);

        JButton confirm = new JButton(confirmAction);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        add(confirm, constraints);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        Dimension dimension = separator.getPreferredSize();
        dimension.height = 3;
        separator.setPreferredSize(dimension);
        add(separator, constraints);

        JButton login = new JButton("Login instead");
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        login.addActionListener(e -> {
            username = "";
            password = "";
            dispose();
            loginListener.actionPerformed(e);
        });
        add(login, constraints);

        pack();
    }

    public void addConfirmButtonActionListener(ActionListener listener){
        this.confirmListener = listener;
    }

    public void addLoginButtonActionListener(ActionListener listener){
        this.loginListener = listener;
    }

    public void addCancelListener(WindowAdapter windowAdapter){
        this.cancelListener = windowAdapter;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void showDialog(){
        setVisible(true);
    }

    private void showPasswordsDoNotMatchPane(){
        JOptionPane.showMessageDialog(this, "Passwords do not match", "", JOptionPane.ERROR_MESSAGE);
    }

    private void showPasswordCriteriaNotMetPane(){
        JOptionPane.showMessageDialog(this, "Password does not meet the right criteria.\n Password should:\n   - Have at least 8 characters\n   - at least one upper and one lowercase letter\n   - at least one digit\n   - at least one symbol (e.g.: . ! # - _)", "", JOptionPane.ERROR_MESSAGE);
    }

}