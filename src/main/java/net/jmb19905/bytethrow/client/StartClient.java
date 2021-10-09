package net.jmb19905.bytethrow.client;

import net.jmb19905.bytethrow.client.gui.Window;
import net.jmb19905.bytethrow.client.networking.ClientManager;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.bytethrow.client.util.ThemeManager;
import net.jmb19905.bytethrow.common.RegistryManager;
import net.jmb19905.bytethrow.common.Version;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.Util;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import javax.swing.*;
import java.net.ConnectException;
import java.util.Arrays;

public class StartClient {

    public static ConfigManager.ClientConfig config;

    public static ClientManager manager;
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
        Logger.info("Starting ByteThrow Messenger Client - Version: " + version);
        if(isDevEnv){
            Logger.info("Is in DEV Environment");
        }

        ShutdownManager.addCleanUp(() -> {
            StartClient.window.setEnabled(false);
            StartClient.window.dispose();
            manager.stop();
            ConfigManager.saveClientConfig();
            Logger.close();
        });

        RegistryManager.registerAll();

        ConfigManager.init();
        config = ConfigManager.loadClientConfig();
        Logger.info("Loaded configs from: " + ConfigManager.getConfigPath());

        Localisation.reload();

        //On Some Linux Systems Java doesn't automatically use Anti-Aliasing
        System.setProperty("awt.useSystemAAFontSettings","on");

        ThemeManager.init();
        try {
            manager = new ClientManager(config.server, config.port);
            window = new Window();
            manager.start();
        }catch (ConnectException e) {
            JOptionPane.showMessageDialog(StartClient.window, Localisation.get("no_internet"), "", JOptionPane.ERROR_MESSAGE);
            ShutdownManager.shutdown(0);
        }catch (Exception e){
            Logger.error(e);
            window.appendLine("Error: " + e.getMessage());
        }
    }

}
