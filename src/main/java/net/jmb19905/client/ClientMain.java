package net.jmb19905.client;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.client.gui.Window;
import net.jmb19905.common.util.Logger;

import javax.swing.*;

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
            client = new Client("localhost", 10101);
            client.start();
        }catch (Exception e){
            window.appendLine("Error: " + e.getMessage());
        }
    }

}
