package net.jmb19905.client.gui;

import net.jmb19905.client.Client;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.util.Logger;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The Window the Client sees
 */
public class Window extends JFrame {

    private final JList<String> list;
    private final JButton addPeer;

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
        listModel.add(0, "Jared");

        list.addListSelectionListener(e -> {

        });
        peerPanel.add(list, BorderLayout.CENTER);

        addPeer = new JButton("Add Peer...");
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
            try {
                String text = field.getText();
                ClientMain.client.sendMessage(getSelectedPeer(), text);
                appendLine("<You> " + text);
            }catch (NullPointerException ex){
                appendLine("Cannot send message! This is a GUI Test!");
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
        listModel.addAll(java.util.List.of(names));
    }

    /**
     * @return the current selected peer name
     */
    public String getSelectedPeer(){
        return list.getSelectedValue();
    }

}
