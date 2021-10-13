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

import net.jmb19905.bytethrow.client.GUIManager;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.gui.components.HintPasswordField;
import net.jmb19905.bytethrow.client.gui.components.HintTextField;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.util.AsynchronousInitializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Show a Login Dialog
 */
public class LoginDialog extends JDialog {

    protected final JTextField usernameInputField;
    protected JPasswordField passwordInputField = null;

    protected ActionListener confirmListener = null;
    protected ActionListener registerListener = null;
    protected WindowAdapter cancelListener = null;

    protected String username = "";
    protected String password = "";

    public LoginDialog(String usernameText, String passwordText, String extraText, boolean showRegisterButton, Window window){
        super(window);
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
            rememberLogin.setSelected(StartClient.config.autoLogin);
            constraints.gridy = 3;
            rememberLogin.addActionListener(l -> StartClient.config.autoLogin = true);
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
        setLocationRelativeTo(null);
    }

    protected void registerActionPerformed(ActionEvent e) {
        username = "";
        password = "";
        setVisible(false);
        if(registerListener != null) {
            registerListener.actionPerformed(e);
        }
        clearTextFields();
    }

    protected void cancelActionPerformed(WindowEvent e) {
        username = "";
        password = "";
        setVisible(false);
        if(cancelListener != null) {
            cancelListener.windowClosing(e);
        }
        clearTextFields();
    }

    protected void confirmActionPerformed(ActionEvent e) {
        username = usernameInputField.getText();
        password = new String(passwordInputField.getPassword());
        setVisible(false);
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

    public LoginDataResult showDialog(){
        AsynchronousInitializer<LoginDataResult> initializer = new AsynchronousInitializer<>();
        SwingUtilities.invokeLater(() -> {
            addConfirmButtonActionListener(evt -> initializer.init(new LoginDataResult(new LoginData(username, password), GUIManager.ResultType.CONFIRM)));
            addCancelListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    initializer.init(new LoginDataResult(new LoginData(username, password), GUIManager.ResultType.CANCEL));
                }
            });
            addRegisterButtonActionListener(evt -> initializer.init(new LoginDataResult(new LoginData(username, password), GUIManager.ResultType.OTHER)));
            setVisible(true);
        });
        return initializer.get();
    }

    public static record LoginData(String username, String password){ }

    public record LoginDataResult(LoginData loginData, GUIManager.ResultType resultType) {}

}