package net.jmb19905.client.gui;

import net.jmb19905.client.ClientMain;
import net.jmb19905.client.ResourceUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.Util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

/**
 * The Window the Client sees
 */
public class Window extends JFrame {

    private final JList<String> list;

    private final JTextPane area;
    private final StyledDocument document;
    private final JTextField field;

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

        DefaultListModel<String> listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.addListSelectionListener(e -> {

        });
        peerPanel.add(list, BorderLayout.CENTER);

        JButton addPeer = new JButton("Add Peer...");
        addPeer.addActionListener(l -> {
            String name = JOptionPane.showInputDialog("Name of the Peer:");
            if(ClientMain.client != null) {
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

        setVisible(true);
        pack();

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
        DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
        listModel.removeAllElements();
        for(String name : names) {
            listModel.addElement(name + " ✗");
        }
    }

    public void addPeer(String peerName){
        DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
        listModel.addElement(peerName + " ✗");
    }

    public void setPeerStatus(String name, boolean status){
        DefaultListModel<String> listModel = ((DefaultListModel<String>) list.getModel());
        try {
            int index;
            String modifiedName;
            if (status) {
                modifiedName = name + " ✓";
                index = listModel.indexOf(name + " ✗");
            } else {
                modifiedName = name + " ✗";
                index = listModel.indexOf(name + " ✓");
            }
            listModel.set(index, modifiedName);
        }catch (IndexOutOfBoundsException ignored){}
    }

    public SimpleAttributeSet getBold() {
        return bold;
    }

    /**
     * @return the current selected peer name
     */
    public String getSelectedPeer(){
        return list.getSelectedValue().replace("✓", "").replace("✗", "").strip();
    }

}
