package net.jmb19905.messenger.client.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.util.ConfigManager;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

/**
 * The Settings Window of the Client - used to change config
 */
public class SettingsWindow extends JDialog {

    public SettingsWindow() {
        setTitle("Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(450, 600));
        setIconImage(Util.getImageResource("icon.png"));

        JPanel contentPanel = new JPanel(new GridBagLayout());

        JScrollPane pane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel themeLabel = new JLabel("Theme: ");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(themeLabel, constraints);

        String[] themes = {"Metal", "Nimbus", "Darcula", "Light", "Dark", "IntelliJ"};
        JComboBox<String> themeCombo = new JComboBox<>(themes);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(themeCombo, constraints);

        themeCombo.setSelectedItem(EncryptedMessenger.clientConfig.theme);
        themeCombo.addItemListener(e -> setLookAndFeel((String) themeCombo.getSelectedItem()));

        JLabel autoLoginLabel = new JLabel("Automatic Login: ");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(autoLoginLabel, constraints);

        JCheckBox autoLoginCheckBox = new JCheckBox();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(autoLoginCheckBox, constraints);

        autoLoginCheckBox.setSelected(EncryptedMessenger.clientConfig.autoLogin);
        autoLoginCheckBox.addActionListener((e) -> {
            EncryptedMessenger.clientConfig.autoLogin = autoLoginCheckBox.isSelected();
            ConfigManager.saveClientConfig(EncryptedMessenger.clientConfig, "config/client_config.json");
        });

        JButton resetSettings = new JButton("Reset Settings");
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(resetSettings, constraints);

        resetSettings.addActionListener(e -> {
            EncryptedMessenger.clientConfig = new ConfigManager.ClientConfig();
            ConfigManager.saveClientConfig(EncryptedMessenger.clientConfig, "config/client_config.json");
            themeCombo.setSelectedItem(EncryptedMessenger.clientConfig.theme);
            setLookAndFeel((String) themeCombo.getSelectedItem());
            autoLoginCheckBox.setSelected(EncryptedMessenger.clientConfig.autoLogin);
        });

        add(pane);

        pack();
    }

    /**
     * Modifies the LookAndFeel of all Swing Object and repaints all Swing Objects
     * @param lookAndFeelName the Name of the LookAndFeel
     */
    public static void setLookAndFeel(String lookAndFeelName) {
        try {
            switch (lookAndFeelName) {
                case "Metal":
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                    break;
                case "Nimbus":
                    UIManager.setLookAndFeel(new NimbusLookAndFeel());
                    break;
                case "Darcula":
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                    break;
                case "Light":
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;
                case "Dark":
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
                case "IntelliJ":
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    break;
            }
            EncryptedMessenger.clientConfig.theme = lookAndFeelName;
        } catch (UnsupportedLookAndFeelException ex) {
            EMLogger.warn("MessagingClient", "Could not change look and feel. Skipping", ex);
        }
        try {
            SwingUtilities.updateComponentTreeUI(EncryptedMessenger.window.settingsWindow);
            SwingUtilities.updateComponentTreeUI(EncryptedMessenger.window);
            EncryptedMessenger.window.settingsWindow.revalidate();
            EncryptedMessenger.window.revalidate();
            EncryptedMessenger.window.settingsWindow.repaint();
            EncryptedMessenger.window.repaint();
            EncryptedMessenger.window.settingsWindow.pack();
            EncryptedMessenger.window.pack();
        } catch (NullPointerException ignored) {
        }
        ConfigManager.saveClientConfig(EncryptedMessenger.clientConfig, "config/client_config.json");
    }

    /**
     * Checks if the LookAndFeel is Bright - is used for Widgets that have to change color depending on the brightness of the LookAndFeel
     * @return if the LookAndFeel is Bright
     */
    public static boolean isLight() {
        return !(UIManager.getLookAndFeel() instanceof FlatDarculaLaf) && !(UIManager.getLookAndFeel() instanceof FlatDarkLaf);
    }

}
