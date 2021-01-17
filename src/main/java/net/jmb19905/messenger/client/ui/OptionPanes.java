package net.jmb19905.messenger.client.ui;

import net.jmb19905.messenger.client.EncryptedMessenger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class OptionPanes {

    public static OutputValue showLoginDialog(ActionListener registerEvent) {
        final OutputValue[] value = new OutputValue[1];
        JDialog dialog = new JDialog(EncryptedMessenger.window);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setLayout(new GridBagLayout());
        dialog.setPreferredSize(new Dimension(350, 250));
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setTitle("Login");

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                value[0] = new OutputValue(OutputValue.CANCEL_OPTION, new String[0]);
                dialog.dispose();
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel usernameLabel = new JLabel("Username:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        dialog.add(usernameLabel, constraints);

        JLabel passwordLabel = new JLabel("Password:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        dialog.add(passwordLabel, constraints);

        JTextField usernameInputField = new JTextField();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        dialog.add(usernameInputField, constraints);

        JPasswordField passwordInputField = new JPasswordField();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        dialog.add(passwordInputField, constraints);

        JButton confirm = new JButton("Confirm");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        confirm.addActionListener(e -> {
            value[0] = new OutputValue(OutputValue.CONFIRM_OPTION, new String[]{usernameInputField.getText(), String.valueOf(passwordInputField.getPassword())});
            dialog.dispose();
        });
        dialog.add(confirm, constraints);

        JButton register = new JButton("Register");
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        register.addActionListener((e) -> {
            value[0] = new OutputValue(OutputValue.SECOND_OPTION, new String[0]);
            dialog.dispose();
            registerEvent.actionPerformed(e);
        });
        dialog.add(register, constraints);

        JButton cancel = new JButton("Cancel");
        constraints.gridx = 3;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        cancel.addActionListener(e -> {
            value[0] = new OutputValue(OutputValue.CANCEL_OPTION, new String[0]);
            dialog.dispose();
        });
        dialog.add(cancel, constraints);

        dialog.pack();
        dialog.setVisible(true);

        //Loop until event is triggered
        while (dialog.isShowing()) {
            dialog.validate();
        }
        return value[0];
    }

    public static OutputValue showRegisterDialog(ActionListener loginListener) {
        final OutputValue[] value = new OutputValue[1];
        JDialog dialog = new JDialog(EncryptedMessenger.window);
        dialog.setModal(true);
        dialog.setLayout(new GridBagLayout());
        dialog.setPreferredSize(new Dimension(400, 250));
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setTitle("Register");

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                value[0] = new OutputValue(OutputValue.CANCEL_OPTION, new String[0]);
                dialog.dispose();
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel usernameLabel = new JLabel("Username:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        dialog.add(usernameLabel, constraints);

        JLabel passwordLabel1 = new JLabel("Password:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        dialog.add(passwordLabel1, constraints);

        JLabel passwordLabel2 = new JLabel("Repeat Password:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        dialog.add(passwordLabel2, constraints);

        JTextField usernameInputField = new JTextField();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        dialog.add(usernameInputField, constraints);

        JPasswordField passwordInputField1 = new JPasswordField();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        dialog.add(passwordInputField1, constraints);

        JPasswordField passwordInputField2 = new JPasswordField();
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        dialog.add(passwordInputField2, constraints);

        JButton confirm = new JButton("Confirm");
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        confirm.addActionListener(e -> {
            if (usernameInputField.getText().length() < 4) {
                JOptionPane.showMessageDialog(null, "The Username is too short", "", JOptionPane.ERROR_MESSAGE);
            } else if (passwordInputField1.getPassword().length < 5) {
                JOptionPane.showMessageDialog(null, "The Password is too short", "", JOptionPane.ERROR_MESSAGE);
            } else {
                if (!Arrays.equals(passwordInputField1.getPassword(), passwordInputField2.getPassword())) {
                    JOptionPane.showMessageDialog(null, "The Passwords do not match", "", JOptionPane.ERROR_MESSAGE);
                } else {
                    value[0] = new OutputValue(OutputValue.CONFIRM_OPTION, new String[]{usernameInputField.getText(), String.valueOf(passwordInputField1.getPassword())});
                    dialog.dispose();
                }
            }
        });
        dialog.add(confirm, constraints);

        JButton login = new JButton("Login");
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        login.addActionListener(e -> {
            value[0] = new OutputValue(OutputValue.SECOND_OPTION, new String[0]);
            dialog.dispose();
            loginListener.actionPerformed(e);
        });
        dialog.add(login, constraints);

        JButton cancel = new JButton("Cancel");
        constraints.gridx = 2;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        cancel.addActionListener(e -> {
            value[0] = new OutputValue(OutputValue.CANCEL_OPTION, new String[0]);
            dialog.dispose();
        });
        dialog.add(cancel, constraints);

        dialog.pack();
        dialog.setVisible(true);

        //Loop until event is triggered
        while (dialog.isShowing()) {
            dialog.validate();
        }
        return value[0];
    }

    public static class OutputValue {

        public static final int CONFIRM_OPTION = 0;
        public static final int CANCEL_OPTION = 1;
        public static final int SECOND_OPTION = 2;

        public int id;
        public String[] values;

        public OutputValue(int id, String[] values) {
            this.id = id;
            this.values = values;
        }

        @Override
        public String toString() {
            return "OutputValue{" +
                    "id=" + id +
                    ", values=" + Arrays.toString(values) +
                    '}';
        }
    }

}
