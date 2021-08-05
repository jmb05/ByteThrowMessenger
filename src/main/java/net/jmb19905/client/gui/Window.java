package net.jmb19905.client.gui;

import net.jmb19905.client.ClientMain;
import net.jmb19905.client.ResourceUtility;
import net.jmb19905.client.gui.components.PicturePanel;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Logger;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    private final SimpleAttributeSet bold;
    private final SimpleAttributeSet italic;
    private final SimpleAttributeSet underline;

    /**
     * Initializes the components
     */
    public Window(){
        this.area = new JTextPane();
        this.document = area.getStyledDocument();
        this.field = new JTextField();
        this.loadingPanel = new PicturePanel(new ImageIcon(ResourceUtility.getResourceAsURL("spinner.gif")));
        setGlassPane(loadingPanel);
        loadingPanel.setVisible(true);

        bold = new SimpleAttributeSet();
        bold.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

        italic = new SimpleAttributeSet();
        italic.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);

        underline = new SimpleAttributeSet();
        underline.addAttribute(StyleConstants.CharacterConstants.Underline, Boolean.TRUE);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(750, 500));

        JPanel messagingPanel = new JPanel(new GridBagLayout());
        JPanel peerPanel = new JPanel(new BorderLayout(0,0));

        list = new PeerList();
        peerPanel.add(list, BorderLayout.CENTER);

        JButton addPeer = new JButton("Add Peer...");
        addPeer.addActionListener(l -> {
            String name = JOptionPane.showInputDialog("Name of the Peer:");
            if(ClientMain.client != null && name != null && !name.equals("")) {
                ClientMain.client.connectToPeer(name);
            }
        });
        peerPanel.add(addPeer, BorderLayout.SOUTH);

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
        messagingPanel.add(field, constraints);

        field.addActionListener(l ->  {
            if(ClientMain.client != null){
                if(list.getSelectedValue() != null) {
                    String text = field.getText();
                    if(ClientMain.client.sendMessage(getSelectedPeer(), text)) {
                        appendMessage("You", text);
                    }else {
                        JOptionPane.showMessageDialog(this, "Cannot send message! Peer not connected!");
                    }
                }else {
                    appendLine("Please select a peer to send the message to!");
                }
            } else {
                appendMessage("You to GUITest", field.getText());
            }
            field.setText("");
        });

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

}
