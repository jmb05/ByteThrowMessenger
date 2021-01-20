package net.jmb19905.messenger.client.ui.util.dialoges;

import net.jmb19905.messenger.client.ui.util.component.HintPasswordField;
import net.jmb19905.messenger.client.ui.util.component.HintTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {
    private ActionListener confirmListener = e -> {};
    private ActionListener registerListener = e -> {};
    private WindowAdapter cancelListener = new WindowAdapter() {};

    private String username = "";
    private String password = "";

    public LoginDialog(){
        setModal(true);
        setResizable(false);
        setLayout(new GridBagLayout());

        setPreferredSize(new Dimension(250, 250));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Login");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                username = "";
                password = "";
                dispose();
                cancelListener.windowClosing(e);
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 15, 5, 15);

        JTextField usernameInputField = new HintTextField("Username");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        add(usernameInputField, constraints);

        JPasswordField passwordInputField = new HintPasswordField("Password");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        add(passwordInputField, constraints);

        JButton confirm = new JButton("Confirm");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        confirm.addActionListener(e -> {
            username = usernameInputField.getText();
            password = new String(passwordInputField.getPassword());
            dispose();
            confirmListener.actionPerformed(e);
        });
        add(confirm, constraints);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        Dimension dimension = separator.getPreferredSize();
        dimension.height = 3;
        separator.setPreferredSize(dimension);
        add(separator, constraints);

        JButton register = new JButton("Register instead");
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        register.addActionListener(e -> {
            username = "";
            password = "";
            dispose();
            registerListener.actionPerformed(e);
        });
        add(register, constraints);

        pack();
        setVisible(true);
    }

    public void addConfirmButtonActionListener(ActionListener listener){
        this.confirmListener = listener;
    }

    public void addRegisterButtonActionListener(ActionListener listener){
        this.registerListener = listener;
    }

    public void addWindowListener(WindowAdapter windowAdapter){
        this.cancelListener = windowAdapter;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}