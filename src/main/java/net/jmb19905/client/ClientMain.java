package net.jmb19905.client;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.client.gui.Window;
import net.jmb19905.common.Version;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.Util;

import javax.swing.*;
import java.awt.*;
import java.net.ConnectException;

public class ClientMain {

    public static ConfigManager.ClientConfig config;

    public static Client client;
    public static Window window;

    public static Version version;

    /**
     * Starts the Client and it's Window
     */
    public static void main(String[] args) {
        version = Util.loadVersion(args[0].equals("dev"));
        Logger.log("Starting ByteThrow Messenger Client - Version: " + version, Logger.Level.INFO);
        config = ConfigManager.loadClientConfigFile("config/client_config.json");
        Logger.log("Loaded configs", Logger.Level.INFO);
        FlatDarculaLaf.setup();
        try {
            window = new Window();
            client = new Client(config.server, config.port);
            client.start();
        }/*catch (ConnectException e) {
            JOptionPane.showMessageDialog(ClientMain.window, "Could not connect to Server! Check your Internet Connection!", "", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }*/catch (Exception e){
            window.appendLine("Error: " + e.getMessage());
        }
    }

}
