package net.jmb19905.gui;

import net.jmb19905.networking.client.Client;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    private final JTextArea area;
    private final JTextField field;

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
            Client.sendMessage(text);
            appendLine("<You> " + text);
            field.setText("");
        });

        setVisible(true);
        pack();
    }

    public void appendLine(String line){
        area.append(line + "\n");
    }


}
