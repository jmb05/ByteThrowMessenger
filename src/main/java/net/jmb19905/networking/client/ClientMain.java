package net.jmb19905.networking.client;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.gui.Window;
import net.jmb19905.util.Logger;

import javax.swing.*;
import java.net.ConnectException;

public class ClientMain {

    public static Client client;
    public static Window window;

    /**
     * Starts the Client and it's Window
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            Logger.log(e, "GUI may not work correctly!", Logger.Level.ERROR);
        }
        try {
            String name = JOptionPane.showInputDialog(null, "Input your name:");
            window = new Window();
            client = new Client(name, "localhost", 10101);
            client.start();
        }catch (Exception e){
            window.appendLine("Error: " + e.getMessage());
        }
    }

}
