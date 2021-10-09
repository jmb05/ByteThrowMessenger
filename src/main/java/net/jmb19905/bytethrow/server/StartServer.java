package net.jmb19905.bytethrow.server;


import net.jmb19905.bytethrow.common.RegistryManager;
import net.jmb19905.bytethrow.common.Version;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.Util;
import net.jmb19905.bytethrow.server.networking.ServerManager;
import net.jmb19905.util.Logger;

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
        Logger.log("Starting ByteThrow Messenger Server - Version: " + version, Logger.Level.INFO);
        if(isDevEnv){
            Logger.log("Is in DEV Environment", Logger.Level.INFO);
        }
        RegistryManager.registerAll();
        config = ConfigManager.loadServerConfigFile("config/server_config.json");
        Logger.log("Loaded configs", Logger.Level.INFO);

        File file = new File("clientData/");
        if(!file.exists() || !file.isDirectory()){
            file.mkdir();
        }
        manager = new ServerManager(config.port);
        manager.start();
    }
}
