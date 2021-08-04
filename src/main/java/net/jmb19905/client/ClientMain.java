package net.jmb19905.client;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.client.gui.Window;
import net.jmb19905.common.util.Logger;

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
            window = new Window();
            client = new Client("192.168.178.10", 10102);
            client.start();
        }catch (ConnectException e) {
            JOptionPane.showMessageDialog(ClientMain.window, "Could not connect to Server! Check your Internet Connection!", "", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }catch (Exception e){
            window.appendLine("Error: " + e.getMessage());
        }
    }

}
