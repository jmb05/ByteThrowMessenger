package net.jmb19905.client.gui.settings;

import net.jmb19905.client.ClientMain;
import net.jmb19905.client.util.Localisation;
import net.jmb19905.common.util.ResourceUtility;
import net.jmb19905.client.util.ThemeManager;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Logger;

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
        setTitle("Settings");
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

        themeCombo.setSelectedItem(ClientMain.config.theme);
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

        autoLoginCheckBox.setSelected(ClientMain.config.autoLogin);
        autoLoginCheckBox.addActionListener((e) -> {
            ClientMain.config.autoLogin = autoLoginCheckBox.isSelected();
            ConfigManager.saveClientConfig(ClientMain.config, "config/client_config.json");
        });

        langLabel = new JLabel(Localisation.get("lang") + ": ");
        constraints.gridx = 0;
        constraints.gridy = 2;
        contentPanel.add(langLabel, constraints);

        langCombo = new JComboBox<>(Localisation.getLocales());
        langCombo.setSelectedItem(ClientMain.config.lang);
        langCombo.addItemListener(l -> {
            ClientMain.config.lang = (String) langCombo.getSelectedItem();
            ConfigManager.saveClientConfig(ClientMain.config, "config/client_config.json");
            Localisation.reload();
            ClientMain.window.repaint();
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
            ClientMain.config = new ConfigManager.ClientConfig();
            ConfigManager.saveClientConfig(ClientMain.config, "config/client_config.json");
            themeCombo.setSelectedItem(ClientMain.config.theme);
            setLookAndFeel((String) themeCombo.getSelectedItem());
            autoLoginCheckBox.setSelected(ClientMain.config.autoLogin);
        });

        add(pane);

        pack();
    }

    /**
     * Modifies the LookAndFeel of all Swing Object and repaints all Swing Objects
     * @param lookAndFeelName the Name of the LookAndFeel
     */
    public static void setLookAndFeel(String lookAndFeelName) {
        ClientMain.config.theme = lookAndFeelName;
        ThemeManager.init();
        try {
            SwingUtilities.updateComponentTreeUI(ClientMain.window.getSettingsWindow());
            SwingUtilities.updateComponentTreeUI(ClientMain.window);
            ClientMain.window.getSettingsWindow().repaint();
            ClientMain.window.repaint();
            ClientMain.window.getSettingsWindow().pack();
            ClientMain.window.pack();
        } catch (NullPointerException e) {
            Logger.log(e, Logger.Level.WARN);
        }
        ConfigManager.saveClientConfig(ClientMain.config, "config/client_config.json");
    }

    public void reloadLang(){
        themeLabel.setText(Localisation.get("theme") + ": ");
        autoLoginLabel.setText(Localisation.get("automatic_login") + ": ");
        langLabel.setText(Localisation.get("lang") + ": ");
        resetSettings.setText(Localisation.get("reset_settings"));
    }

}
