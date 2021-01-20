package net.jmb19905.messenger.client.ui.settings;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.util.*;
import net.jmb19905.messenger.util.config.ConfigManager;
import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

/**
 * The Settings Window of the Client - used to change config
 */
public class SettingsWindow extends JDialog {

    private final JLabel themeLabel;
    private final JComboBox<String> themeCombo;
    private final JLabel autoLoginLabel;
    private final JCheckBox autoLoginCheckBox;
    private final JLabel fontLabel;
    private final JComboBox<Font> fontCombo;
    private final JLabel fontSizeLabel;
    private final JSpinner fontSizeSpinner;
    private final JButton resetSettings;

    public SettingsWindow() {
        setTitle("Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(FileUtility.getImageResource("icon.png"));

        JPanel contentPanel = new JPanel(new GridBagLayout());

        JScrollPane pane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new Insets(10, 10, 10, 10);

        themeLabel = new JLabel("Theme: ");
        themeLabel.setFont(Variables.defaultFont);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(themeLabel, constraints);

        String[] themes = {"Metal", "Nimbus", "Darcula", "Light", "Dark", "IntelliJ"};
        themeCombo = new JComboBox<>(themes);
        themeCombo.setFont(Variables.defaultFont);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(themeCombo, constraints);

        themeCombo.setSelectedItem(ByteThrowClient.clientConfig.theme);
        themeCombo.addItemListener(e -> setLookAndFeel((String) themeCombo.getSelectedItem()));

        autoLoginLabel = new JLabel("Automatic Login: ");
        autoLoginLabel.setFont(Variables.defaultFont);
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

        autoLoginCheckBox.setSelected(ByteThrowClient.clientConfig.autoLogin);
        autoLoginCheckBox.addActionListener((e) -> {
            ByteThrowClient.clientConfig.autoLogin = autoLoginCheckBox.isSelected();
            ConfigManager.saveClientConfig(ByteThrowClient.clientConfig, "config/client_config.json");
        });

        fontLabel = new JLabel("Font: ");
        fontLabel.setFont(Variables.defaultFont);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(fontLabel, constraints);

        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = graphicsEnvironment.getAllFonts();

        fontCombo = new JComboBox<>(allFonts);
        fontCombo.setFont(Variables.defaultFont);
        fontCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value != null) {
                    Font font = (Font) value;
                    value = font.getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        for(Font font : allFonts){
            if(font.getName().equals(ByteThrowClient.clientConfig.font)){
                fontCombo.setSelectedItem(font);
                break;
            }
        }
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(fontCombo, constraints);

        fontCombo.addItemListener(e -> {
            ByteThrowClient.clientConfig.font = ((Font) fontCombo.getSelectedItem()).getName();
            Variables.initFonts();
            ByteThrowClient.window.repaint();
            repaint();
            ByteThrowClient.window.pack();
            pack();
        });

        fontSizeLabel = new JLabel("Font size: ");
        fontSizeLabel.setFont(Variables.defaultFont);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(fontSizeLabel, constraints);

        SpinnerModel fontSizeSpinnerModel = new SpinnerNumberModel(18, 10, 30, 1);
        fontSizeSpinner = new JSpinner(fontSizeSpinnerModel);
        fontSizeSpinner.setFont(Variables.defaultFont);
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(fontSizeSpinner, constraints);

        fontSizeSpinner.addChangeListener(e -> {
            ByteThrowClient.clientConfig.fontSize = (int) fontSizeSpinner.getValue();
            Variables.initFonts();
            ByteThrowClient.window.repaint();
            repaint();
            ByteThrowClient.window.pack();
            pack();
        });

        resetSettings = new JButton("Reset Settings");
        resetSettings.setFont(Variables.defaultFont);
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        contentPanel.add(resetSettings, constraints);

        resetSettings.addActionListener(e -> {
            ByteThrowClient.clientConfig = new ConfigManager.ClientConfig();
            ConfigManager.saveClientConfig(ByteThrowClient.clientConfig, "config/client_config.json");
            themeCombo.setSelectedItem(ByteThrowClient.clientConfig.theme);
            setLookAndFeel((String) themeCombo.getSelectedItem());
            autoLoginCheckBox.setSelected(ByteThrowClient.clientConfig.autoLogin);
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
            ByteThrowClient.clientConfig.theme = lookAndFeelName;
        } catch (UnsupportedLookAndFeelException ex) {
            BTMLogger.warn("MessagingClient", "Could not change look and feel. Skipping", ex);
        }
        try {
            SwingUtilities.updateComponentTreeUI(ByteThrowClient.window.settingsWindow);
            SwingUtilities.updateComponentTreeUI(ByteThrowClient.window);
            ByteThrowClient.window.settingsWindow.repaint();
            ByteThrowClient.window.repaint();
            ByteThrowClient.window.settingsWindow.pack();
            ByteThrowClient.window.pack();
        } catch (NullPointerException ignored) {
        }
        ConfigManager.saveClientConfig(ByteThrowClient.clientConfig, "config/client_config.json");
    }

    /**
     * Checks if the LookAndFeel is Bright - is used for Widgets that have to change color depending on the brightness of the LookAndFeel
     * @return if the LookAndFeel is Bright
     */
    public static boolean isLight() {
        return !(UIManager.getLookAndFeel() instanceof FlatDarculaLaf) && !(UIManager.getLookAndFeel() instanceof FlatDarkLaf);
    }

    @Override
    public void repaint() {
        super.repaint();
        themeLabel.setFont(Variables.defaultFont);
        themeCombo.setFont(Variables.defaultFont);
        autoLoginLabel.setFont(Variables.defaultFont);
        autoLoginCheckBox.setFont(Variables.defaultFont);
        fontLabel.setFont(Variables.defaultFont);
        fontCombo.setFont(Variables.defaultFont);
        fontSizeLabel.setFont(Variables.defaultFont);
        fontSizeSpinner.setFont(Variables.defaultFont);
        resetSettings.setFont(Variables.defaultFont);
    }
}
