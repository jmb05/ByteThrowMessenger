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
import java.util.Arrays;

public class StartClient {

    public static ConfigManager.ClientConfig config;

    public static Client client;
    public static Window window;

    public static Version version;

    public static boolean isDevEnv = false;

    /**
     * Starts the Client and it's Window
     */
    public static void main(String[] args) {
        if(args.length > 0){
            isDevEnv = Arrays.asList(args).contains("dev");
        }

        Logger.setLevel(isDevEnv ? Logger.Level.TRACE : Logger.Level.INFO);
        Logger.initLogFile(false);

        version = Util.loadVersion(isDevEnv);
        Logger.log("Starting ByteThrow Messenger Client - Version: " + version, Logger.Level.INFO);
        if(isDevEnv){
            Logger.log("Is in DEV Environment", Logger.Level.INFO);
        }

        ConfigManager.init();
        config = ConfigManager.loadClientConfig();
        Logger.log("Loaded configs from: " + ConfigManager.getConfigPath(), Logger.Level.INFO);

        Localisation.reload();

        //On Some Linux Systems Java doesn't automatically use Anti-Aliasing
        System.setProperty("awt.useSystemAAFontSettings","on");

        ThemeManager.init();
        try {
            client = new Client(config.server, config.port);
            window = new Window();
            client.start();
        }catch (ConnectException e) {
            JOptionPane.showMessageDialog(StartClient.window, Localisation.get("no_internet"), "", JOptionPane.ERROR_MESSAGE);
            exit(0);
        }catch (Exception e){
            Logger.log(e, Logger.Level.ERROR);
            window.appendLine("Error: " + e.getMessage());
        }
    }

    public static void exit(int code, String message, boolean disconnect){
        StartClient.window.setEnabled(false);
        if(disconnect){
            StartClient.window.appendLine(Localisation.get("disconnect"));
        }
        StartClient.window.appendLine(message);
        StartClient.window.dispose();
        exit(code);
    }

    public static void exit(int code){
        client.stop();
        ConfigManager.saveClientConfig();
        Logger.close();
        System.exit(code);
    }

}
