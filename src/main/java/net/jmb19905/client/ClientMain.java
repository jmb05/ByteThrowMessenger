package net.jmb19905.client;

import net.jmb19905.client.gui.Window;
import net.jmb19905.client.networking.Client;
import net.jmb19905.client.util.Localisation;
import net.jmb19905.client.util.ThemeManager;
import net.jmb19905.common.Version;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.Util;

import javax.swing.*;
import java.net.ConnectException;

public class ClientMain {

    public static ConfigManager.ClientConfig config;

    public static Client client;
    public static Window window;

    public static Version version;

    public static boolean isDevEnv;

    /**
     * Starts the Client and it's Window
     */
    public static void main(String[] args) {
        isDevEnv = args.length > 0;
        version = Util.loadVersion(isDevEnv);
        Logger.log("Starting ByteThrow Messenger Client - Version: " + version, Logger.Level.INFO);
        config = ConfigManager.loadClientConfigFile("config/client_config.json");
        Logger.log("Loaded configs", Logger.Level.INFO);
        Localisation.reload();
        ThemeManager.init();
        try {
            client = new Client(config.server, config.port);
            window = new Window();
            client.start();
        }catch (ConnectException e) {
            JOptionPane.showMessageDialog(ClientMain.window, Localisation.get("no_internet"), "", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }catch (Exception e){
            Logger.log(e, Logger.Level.ERROR);
            window.appendLine("Error: " + e.getMessage());
        }
    }

}
