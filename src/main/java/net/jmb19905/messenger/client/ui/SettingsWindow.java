package net.jmb19905.messenger.client.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.util.ConfigManager;
import net.jmb19905.messenger.util.EMLogger;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

public class SettingsWindow extends JDialog {

    public SettingsWindow(){
        setTitle("Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(450, 600));
        setIconImage(new ImageIcon("src/main/resources/icon.png").getImage());

        JPanel contentPanel = new JPanel(new GridBagLayout());

        JScrollPane pane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new Insets(10,10,10,10);

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
            ConfigManager.saveConfig(EncryptedMessenger.clientConfig, "client_config.json");
        });

        JButton resetSettings = new JButton("Reset Settings");
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(resetSettings, constraints);

        add(pane);

        pack();
    }

    public static void setLookAndFeel(String lookAndFeelName){
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
        }catch (NullPointerException ignored){}
        ConfigManager.saveConfig(EncryptedMessenger.clientConfig, "client_config.json");
    }

    public static boolean isDark(){
        return UIManager.getLookAndFeel() instanceof FlatDarculaLaf || UIManager.getLookAndFeel() instanceof FlatDarkLaf;
    }

}
