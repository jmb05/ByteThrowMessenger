package net.jmb19905.client.gui;

import net.jmb19905.client.ClientMain;
import net.jmb19905.client.util.Localisation;
import net.jmb19905.client.gui.components.HintPasswordField;
import net.jmb19905.client.gui.components.HintTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginDialog extends JDialog {

    protected final JTextField usernameInputField;
    protected JPasswordField passwordInputField = null;

    protected ActionListener confirmListener = null;
    protected ActionListener registerListener = null;
    protected WindowAdapter cancelListener = null;

    protected String username = "";
    protected String password = "";

    public LoginDialog(String usernameText, String passwordText, String extraText, boolean showRegisterButton){
        setModal(true);
        setResizable(false);
        setLayout(new GridBagLayout());

        setPreferredSize(new Dimension(250, 250));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Localisation.get("login"));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelActionPerformed(e);
            }
        });

        Action confirmAction = new AbstractAction(Localisation.get("confirm")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmActionPerformed(e);
            }
        };

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.WEST;

        JLabel extraInformationLabel = new JLabel(extraText);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        if(extraText.trim().equals("")){
            constraints.insets = new Insets(0, 0, 0, 0);
        }
        add(extraInformationLabel, constraints);

        usernameInputField = new HintTextField(Localisation.get("username"));
        usernameInputField.setText(usernameText);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(5, 15, 5, 15);
        usernameInputField.addActionListener(l -> {
            if(passwordInputField.getPassword().length == 0){
                passwordInputField.requestFocus();
            }else {
                confirmAction.actionPerformed(l);
            }
        });
        add(usernameInputField, constraints);

        passwordInputField = new HintPasswordField(Localisation.get("password"));
        passwordInputField.setText(passwordText);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        passwordInputField.addActionListener(confirmAction);
        add(passwordInputField, constraints);

        if(showRegisterButton) {
            JCheckBox rememberLogin = new JCheckBox(Localisation.get("automatic_login"));
            rememberLogin.setSelected(ClientMain.config.autoLogin);
            constraints.gridy = 3;
            rememberLogin.addActionListener(l -> ClientMain.config.autoLogin = true);
            add(rememberLogin, constraints);
        }

        JButton confirm = new JButton(confirmAction);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        add(confirm, constraints);

        if(showRegisterButton) {
            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
            constraints.gridx = 0;
            constraints.gridy = 5;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.CENTER;
            Dimension dimension = separator.getPreferredSize();
            dimension.height = 3;
            separator.setPreferredSize(dimension);
            add(separator, constraints);

            JButton register = new JButton(Localisation.get("register_instead"));
            constraints.gridx = 0;
            constraints.gridy = 6;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.CENTER;
            register.addActionListener(this::registerActionPerformed);
            add(register, constraints);
        }

        pack();
    }

    protected void registerActionPerformed(ActionEvent e) {
        username = "";
        password = "";
        dispose();
        if(registerListener != null) {
            registerListener.actionPerformed(e);
        }
        clearTextFields();
    }

    protected void cancelActionPerformed(WindowEvent e) {
        username = "";
        password = "";
        dispose();
        if(cancelListener != null) {
            cancelListener.windowClosing(e);
        }
        clearTextFields();
    }

    protected void confirmActionPerformed(ActionEvent e) {
        username = usernameInputField.getText();
        password = new String(passwordInputField.getPassword());
        dispose();
        if(confirmListener != null) {
            confirmListener.actionPerformed(e);
        }
        clearTextFields();
    }

    protected void clearTextFields(){
        usernameInputField.setText("");
        passwordInputField.setText("");
    }

    public void addConfirmButtonActionListener(ActionListener listener){
        this.confirmListener = listener;
    }

    public void addRegisterButtonActionListener(ActionListener listener){
        this.registerListener = listener;
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

}