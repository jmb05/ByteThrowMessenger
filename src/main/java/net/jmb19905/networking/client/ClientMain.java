package net.jmb19905.networking.client;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.gui.Window;
import net.jmb19905.util.Logger;

import javax.swing.*;

public class ClientMain {

    public static Client client;
    public static Window window;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            Logger.log(e, "GUI may not work correctly!", Logger.Level.ERROR);
        }
        String name = JOptionPane.showInputDialog(null, "Input your name:");
        window = new Window();
        client = new Client(name, "localhost", 10101);
        client.start();
    }

}
