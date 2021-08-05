package net.jmb19905.client.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.client.ClientMain;
import net.jmb19905.client.gui.components.PicturePanel;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.Util;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class GUITest {

    public static void main(String[] args) {
        ClientMain.version = Util.loadVersion(args[0].equals("dev"));
        FlatDarculaLaf.setup();
        Window window = new Window();
        window.appendLine("This is a GUI Test");
        window.showLoading(true);

        /*JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new PicturePanel(new ImageIcon("/home/jmb05/IdeaProjects/ByteThrowMessenger/src/main/resources/spinner.gif")));
        frame.setVisible(true);
        frame.pack();*/

    }
}
