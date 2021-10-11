package net.jmb19905.bytethrow.server;


import net.jmb19905.bytethrow.common.RegistryManager;
import net.jmb19905.bytethrow.common.Version;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.Util;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.bytethrow.server.networking.ServerManager;
import net.jmb19905.bytethrow.server.util.ClientDataFilesManager;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import java.io.File;

public class StartServer {

    public static Version version;
    public static ConfigManager.ServerConfig config;

    public static ServerManager manager;

    public static boolean isDevEnv;

    /**
     * Starts the server
     * @param args program arguments
     */
    public static void main(String[] args) {
        isDevEnv = args.length > 0;
        Logger.setLevel(isDevEnv ? Logger.Level.TRACE : Logger.Level.INFO);
        Logger.initLogFile(true);
        version = Util.loadVersion(isDevEnv);
        Logger.info("Starting ByteThrow Messenger Server - Version: " + version);
        if(isDevEnv){
            Logger.info("Is in DEV Environment");
        }
        RegistryManager.registerAll();
        config = ConfigManager.loadServerConfigFile("config/server_config.json");
        Logger.info("Loaded configs");

        DatabaseManager.open();
        ShutdownManager.addCleanUp(DatabaseManager::close);

        manager = new ServerManager(config.port);
        ClientDataFilesManager.loadChats();
        manager.start();
    }
}
