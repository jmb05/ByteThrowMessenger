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

import net.jmb19905.bytethrow.client.ClientConfig;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.util.ThemeManager;
import net.jmb19905.bytethrow.common.util.ResourceUtility;
import net.jmb19905.util.Localisation;
import net.jmb19905.util.config.ConfigManager;
import net.jmb19905.util.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * The Settings Window of the Client - used to change config
 */
public class SettingsWindow extends JDialog {

    private final JLabel themeLabel;
    private final JComboBox<String> themeCombo;
    private final JLabel autoLoginLabel;
    private final JCheckBox autoLoginCheckBox;
    private final JLabel langLabel;
    private final JComboBox<String> langCombo;
    private final JButton resetSettings;

    public SettingsWindow() {
        setTitle(Localisation.get("general_settings"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(ResourceUtility.getImageResource("icons/icon.png"));

        JPanel contentPanel = new JPanel(new GridBagLayout());

        JScrollPane pane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new Insets(10, 10, 10, 10);

        themeLabel = new JLabel(Localisation.get("theme") + ": ");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(themeLabel, constraints);

        String[] themes = {"Darcula", "Dark", "Light", "IntelliJ"};
        themeCombo = new JComboBox<>(themes);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(themeCombo, constraints);

        themeCombo.setSelectedItem(StartClient.config.theme);
        themeCombo.addItemListener(e -> setLookAndFeel((String) themeCombo.getSelectedItem()));

        autoLoginLabel = new JLabel(Localisation.get("automatic_login") + ": ");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(autoLoginLabel, constraints);

        autoLoginCheckBox = new JCheckBox();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(autoLoginCheckBox, constraints);

        autoLoginCheckBox.setSelected(StartClient.config.autoLogin);
        autoLoginCheckBox.addActionListener((e) -> {
            StartClient.config.autoLogin = autoLoginCheckBox.isSelected();
            ConfigManager.saveConfigFile(StartClient.config);
        });

        langLabel = new JLabel(Localisation.get("lang") + ": ");
        constraints.gridx = 0;
        constraints.gridy = 2;
        contentPanel.add(langLabel, constraints);

        langCombo = new JComboBox<>(Localisation.getLocaleNames());
        langCombo.setSelectedItem(StartClient.config.lang);
        langCombo.addItemListener(l -> {
            StartClient.config.lang = (String) langCombo.getSelectedItem();
            ConfigManager.saveConfigFile(StartClient.config);
            Localisation.reload(StartClient.config.lang);
            StartClient.guiManager.repaint();
            pack();
        });
        constraints.gridx = 1;
        contentPanel.add(langCombo, constraints);

        resetSettings = new JButton(Localisation.get("reset_settings"));
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(resetSettings, constraints);

        resetSettings.addActionListener(e -> {
            StartClient.config = new ClientConfig(StartClient.deployState);
            ConfigManager.saveConfigFile(StartClient.config);
            themeCombo.setSelectedItem(StartClient.config.theme);
            setLookAndFeel((String) themeCombo.getSelectedItem());
            autoLoginCheckBox.setSelected(StartClient.config.autoLogin);
            langCombo.setSelectedItem(StartClient.config.lang);
        });

        add(pane);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Modifies the LookAndFeel of all Swing Object and repaints all Swing Objects
     *
     * @param lookAndFeelName the Name of the LookAndFeel
     */
    public static void setLookAndFeel(String lookAndFeelName) {
        StartClient.config.theme = lookAndFeelName;
        ThemeManager.init(StartClient.config);
        try {
            StartClient.guiManager.updateComponentTree();
            StartClient.guiManager.pack();
        } catch (NullPointerException e) {
            Logger.warn(e);
        }
        ConfigManager.saveConfigFile(StartClient.config);
    }

    public void reloadLang() {
        themeLabel.setText(Localisation.get("theme") + ": ");
        autoLoginLabel.setText(Localisation.get("automatic_login") + ": ");
        langLabel.setText(Localisation.get("lang") + ": ");
        resetSettings.setText(Localisation.get("reset_settings"));
        setTitle(Localisation.get("general_settings"));
        pack();
    }

}
