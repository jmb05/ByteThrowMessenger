package net.jmb19905.client.gui;

import net.jmb19905.client.ClientMain;
import net.jmb19905.common.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * The Window the Client sees
 */
public class Window extends JFrame {

    private final JList<String> list;

    private final JTextArea area;
    private final JTextField field;

    /**
     * Initializes the components
     */
    public Window(){
        this.area = new JTextArea();
        this.field = new JTextField();

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
            ClientMain.client.connectToPeer(name);
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
            if(list.getSelectedValue() != null) {
                try {
                    String text = field.getText();
                    if(ClientMain.client.sendMessage(getSelectedPeer(), text)) {
                        appendLine("<You to " + getSelectedPeer() + "> " + text);
                    }else {
                        JOptionPane.showMessageDialog(this, "Cannot send message! Peer not connected!");
                    }
                } catch (NullPointerException ex) {
                    appendLine("Cannot send message! This is a GUI Test!");
                }
                field.setText("");
            }else {
                appendLine("Please select a peer to send the message to!");
            }
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

    /**
     * Appends a String to the Window's TextArea and logs it
     * @param line the String that is appended
     */
    public void appendLine(String line){
        area.append(line + "\n");
        Logger.log(line, Logger.Level.INFO);
    }

    /**
     * Appends a String to the Window's TextArea and logs it with a specific Level
     * @param line the String that is appended
     * @param level the Level (Severity) of the log message
     */
    public void log(String line, Logger.Level level){
        area.append(line + "\n");
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

    /**
     * @return the current selected peer name
     */
    public String getSelectedPeer(){
        return list.getSelectedValue().replace("✓", "").replace("✗", "").strip();
    }

}
