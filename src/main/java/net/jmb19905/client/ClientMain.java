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
        Logger.setLevel(isDevEnv ? Logger.Level.TRACE : Logger.Level.INFO);
        Logger.initLogFile(false);
        version = Util.loadVersion(isDevEnv);
        Logger.log("Starting ByteThrow Messenger Client - Version: " + version, Logger.Level.INFO);
        if(isDevEnv){
            Logger.log("Is in DEV Environment", Logger.Level.INFO);
        }
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
            exit(0);
        }catch (Exception e){
            Logger.log(e, Logger.Level.ERROR);
            window.appendLine("Error: " + e.getMessage());
        }
    }

    public static void exit(int code, String message, boolean disconnect){
        ClientMain.window.setEnabled(false);
        if(disconnect){
            ClientMain.window.appendLine(Localisation.get("disconnect"));
        }
        ClientMain.window.appendLine(message);
        ClientMain.window.dispose();
        exit(code);
    }

    public static void exit(int code){
        client.stop();
        ConfigManager.saveClientConfig(config, "config/client_config.json");
        Logger.close();
        System.exit(code);
    }

}
