package net.jmb19905.client.gui;

import net.jmb19905.client.Client;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.util.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * The Window the Client sees
 */
public class Window extends JFrame {

    private final JTextArea area;
    private final JTextField field;

    /**
     * Initializes the components
     */
    public Window(){
        this.area = new JTextArea();
        this.field = new JTextField();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(750, 500));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(area, constraints);

        area.setEditable(false);

        constraints.gridy = 1;
        constraints.weighty = 0;
        add(field, constraints);

        field.addActionListener(l ->  {
            String text = field.getText();
            ClientMain.client.sendMessage(text);
            appendLine("<You> " + text);
            field.setText("");
        });

        setVisible(true);
        pack();
    }

    /**
     * Appends a String to the Window's TextArea and logs it
     * @param line the String that is appended
     */
    public void appendLine(String line){
        area.append(line + "\n");
        Logger.log(line, Logger.Level.INFO);
    }

    public void log(String line, Logger.Level level){
        area.append(line + "\n");
        Logger.log(line, level);
    }

}
