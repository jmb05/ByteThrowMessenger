package net.jmb19905.messenger.client.ui;

import net.jmb19905.messenger.client.CommandParser;
import net.jmb19905.messenger.client.EncryptedMessenger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Window extends JFrame {

    private final JTextArea area;
    private final JTextField inputField;

    public Window(){
        setTitle("Encrypted Messenger");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 750));
        setLayout(new BorderLayout(5,5));

        Font font = new Font("segoeui", Font.PLAIN, 18);

        area = new JTextArea();
        area.setFont(font);
        area.setEditable(false);
                add(area, BorderLayout.CENTER);
        inputField = new JTextField();
        inputField.setFont(font);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    String command = inputField.getText();
                    inputField.setText("");
                    CommandParser.parseCommand(command, EncryptedMessenger.messagingClient);
                }
            }
        });
        add(inputField, BorderLayout.SOUTH);

        pack();
    }

    public void append(String s){
        area.append(s);
    }

    public void appendLine(String s){
        area.append(s + "\n");
    }

}
