package net.jmb19905.client.gui;

import net.jmb19905.client.ClientMain;
import net.jmb19905.client.gui.settings.AccountSettings;
import net.jmb19905.client.util.Localisation;
import net.jmb19905.client.gui.settings.SettingsWindow;
import net.jmb19905.common.util.ResourceUtility;
import net.jmb19905.client.gui.components.PicturePanel;
import net.jmb19905.client.util.ThemeManager;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.Util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * The Window the Client sees
 */
public class Window extends JFrame {

    private final PeerList list;

    private final JTextPane area;
    private final StyledDocument document;
    private final JTextField field;
    private final PicturePanel loadingPanel;
    private final JToolBar toolbar;
    private final JPanel peerPanel;
    private final JButton addPeer;

    private final SimpleAttributeSet bold;
    private final SimpleAttributeSet italic;
    private final SimpleAttributeSet underline;

    private final SettingsWindow settingsWindow;
    private final AccountSettings accountSettings;

    /**
     * Initializes the components
     */
    public Window(){
        this.area = new JTextPane();
        this.document = area.getStyledDocument();
        this.field = new JTextField();
        this.loadingPanel = new PicturePanel(new ImageIcon(ResourceUtility.getResourceAsURL("icons/spinner.gif")));
        setGlassPane(loadingPanel);
        setIconImage(ResourceUtility.getImageResource("icons/icon.png"));

        bold = new SimpleAttributeSet();
        bold.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

        italic = new SimpleAttributeSet();
        italic.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);

        underline = new SimpleAttributeSet();
        underline.addAttribute(StyleConstants.CharacterConstants.Underline, Boolean.TRUE);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(750, 500));

        JPanel messagingPanel = new JPanel(new GridBagLayout());
        peerPanel = new JPanel(new GridBagLayout());

        GridBagConstraints peerPanelConstraints = new GridBagConstraints();

        list = new PeerList();
        peerPanelConstraints.fill = GridBagConstraints.BOTH;
        peerPanelConstraints.weightx = 1;
        peerPanelConstraints.weighty = 1;
        peerPanel.add(list, peerPanelConstraints);

        addPeer = new JButton(Localisation.get("add_peer"));
        addPeer.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(Localisation.get("peer_name_input"));
            if(ClientMain.client != null && name != null && !name.equals("")) {
                ClientMain.client.connectToPeer(name);
            }
        });
        peerPanelConstraints.gridy = 1;
        peerPanelConstraints.weightx = 0;
        peerPanelConstraints.weighty = 0;
        peerPanelConstraints.insets = new Insets(5,0,5,0);
        peerPanel.add(addPeer, peerPanelConstraints);


        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, peerPanel, messagingPanel);
        pane.setDividerSize(0);
        pane.setDividerLocation(this.getPreferredSize().width / 4);
        add(pane);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        messagingPanel.add(area, constraints);

        area.setEditable(false);

        constraints.gridy = 1;
        constraints.weighty = 0;
        constraints.insets = new Insets(5,0,5,0);
        messagingPanel.add(field, constraints);

        field.addActionListener(l ->  {
            if(ClientMain.client != null){
                if(list.getSelectedValue() != null) {
                    String text = field.getText();
                    if(ClientMain.client.sendMessage(getSelectedPeer(), text)) {
                        appendMessage("You", text);
                    }else {
                        JOptionPane.showMessageDialog(this, Localisation.get("chat_doesnt_exist", getSelectedPeer()));
                    }
                }else {
                    appendLine(Localisation.get("select_peer"));
                }
            } else {
                appendMessage(Localisation.get("you") + " " + Localisation.get("to") + " GUITest", field.getText());
            }
            field.setText("");
        });

        toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolbar.setFloatable(false);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.weightx = 0;
        constraints.weighty = 1;
        constraints.insets = new Insets(0,0,0,0);
        messagingPanel.add(toolbar, constraints);

        initToolBar();

        setTitle("ByteThrow Messenger - " + ClientMain.version);
        setVisible(true);
        pack();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ConfigManager.saveClientConfig(ClientMain.config, "config/client_config.json");
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pane.setDividerLocation(getSize().width / 4);
            }
        });

        this.settingsWindow = new SettingsWindow();

        BufferedImage bufferedImage = ResourceUtility.getImageResource("icons/placeholder.png");
        ImageIcon icon = new ImageIcon(Util.cropImageToCircle(Util.toBufferedImage(bufferedImage.getScaledInstance(128, 128, 0))));
        this.accountSettings = new AccountSettings(icon);
        repaint();
    }

    private void reloadLang(){
        addPeer.setText(Localisation.get("add_peer"));
        settingsWindow.reloadLang();
    }

    public void appendMessage(String name, String messageText) {
        append("<" + name + "> ", bold);
        append(messageText, null);
        newLine();
    }

    public void appendImage(BufferedImage image){
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        imageLabel.setOpaque(false);
        imageLabel.setBackground(Color.CYAN);
        area.setSelectionStart(area.getText().length());
        area.setSelectionEnd(area.getText().length());
        area.insertComponent(imageLabel);
    }

    public void appendCustomStyledText(String text){
        String[] parts = (" " + text + " ").split("__");
        for(int i=0;i<parts.length;i++){
            String textPart = parts[i];
            if(i == 0){
                textPart = textPart.substring(1);
            }if(i == parts.length - 1){
                if(textPart.equals(" ")){
                    parts[i] = "";
                }else {
                    textPart.substring(0, textPart.length() - 2);
                }
            }
            if(i % 2 == 0){
                append(textPart, null);
            }else {
                append(textPart, underline);
            }
        }
    }

    public void append(String text, AttributeSet attributeSet){
        try {
            document.insertString(document.getLength(), text, attributeSet);
        } catch (BadLocationException e) {
            Logger.log(e, Logger.Level.ERROR);
        }
        Logger.logPart(text, Logger.Level.INFO);
    }

    public void newLine(){
        try {
            document.insertString(document.getLength(), "\n", null);
        } catch (BadLocationException e) {
            Logger.log(e, Logger.Level.ERROR);
        }
        Logger.finishLine();
    }

    /**
     * Appends a String to the Window's TextArea and logs it
     * @param line the String that is appended
     */
    public void appendLine(String line){
        try {

            document.insertString(document.getLength(), line + "\n", null);
        } catch (BadLocationException e) {
            Logger.log(e, Logger.Level.ERROR);
        }
        Logger.log(line, Logger.Level.INFO);
    }

    /**
     * Appends a String to the Window's TextArea and logs it with a specific Level
     * @param line the String that is appended
     * @param level the Level (Severity) of the log message
     */
    public void log(String line, Logger.Level level){
        try {
            document.insertString(document.getLength(), line + "\n", null);
        } catch (BadLocationException e) {
            Logger.log(e, Logger.Level.ERROR);
        }
        Logger.log(line, level);
    }

    /**
     * empties the Peer list and add new names
     * @param names the names to be put into the list
     */
    public void setPeers(String[] names){
        list.setPeers(names);
    }

    public void addPeer(String peerName){
        list.addPeer(peerName);
    }

    public void removePeer(String peerName){
        list.removePeer(peerName);
    }

    public void setPeerStatus(String name, boolean status){
        list.setPeerStatus(name, status);
    }

    public SimpleAttributeSet getBold() {
        return bold;
    }

    public void showLoading(boolean loading){
        loadingPanel.setVisible(loading);
    }

    /**
     * @return the current selected peer name
     */
    public String getSelectedPeer(){
        return list.getSelectedValue();
    }

    public SettingsWindow getSettingsWindow() {
        return settingsWindow;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void repaint(){
        toolbar.setBackground(list.getBackground());
        peerPanel.setBackground(list.getBackground());
        reloadLang();
        initActions();
        initToolBar();
    }

    private void initToolBar(){
        toolbar.removeAll();
        toolbar.add(settingsAction);
        toolbar.add(Box.createVerticalGlue());
        toolbar.add(sendAction);
    }

    private Action settingsAction;

    private Action sendAction;

    private void initActions(){
        settingsAction = new AbstractAction("", ThemeManager.getIcon("settings_wheel")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu settingsMenu = new JPopupMenu("Settings");
                JMenuItem settingsMenuItem = new JMenuItem("General Settings");
                settingsMenuItem.addActionListener(l -> settingsWindow.setVisible(true));
                settingsMenu.add(settingsMenuItem);

                JMenuItem accountSettingsMenuItem = new JMenuItem("Account Settings");
                accountSettingsMenuItem.addActionListener(l -> accountSettings.setVisible(true));
                settingsMenu.add(accountSettingsMenuItem);

                settingsMenu.show(toolbar.getComponentAtIndex(0), toolbar.getComponentAtIndex(0).getX(), toolbar.getComponentAtIndex(0).getY());
            }
        };
        sendAction = new AbstractAction("", ThemeManager.getIcon("send")) {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };
    }

}
