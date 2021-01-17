package net.jmb19905.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.client.ui.conversation.ConversationPane;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.util.ConfigManager;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        EMLogger.init();
        JFrame frame = new JFrame("InternalFrame Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 750));

        ConversationPane pane = new ConversationPane();
        frame.add(pane);

        frame.setVisible(true);
        frame.pack();

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {

            }
        });
    }
}
