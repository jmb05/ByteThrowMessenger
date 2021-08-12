package net.jmb19905.server;


import net.jmb19905.common.Version;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.Util;
import net.jmb19905.server.networking.Server;

import java.io.File;
import java.net.BindException;

public class StartServer {

    public static Version version;
    public static ConfigManager.ServerConfig config;

    public static Server server;

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
        config = ConfigManager.loadServerConfigFile("config/server_config.json");
        Logger.log("Loaded configs", Logger.Level.INFO);

        File file = new File("clientData/");
        if(!file.exists() || !file.isDirectory()){
            file.mkdir();
        }
        try {
            server = new Server(config.port);
            server.run();
        }catch (BindException e){
            Logger.log(e, "Could not bind port! Is a server already running?", Logger.Level.FATAL);
            System.exit(-1);
        } catch (Exception e) {
            Logger.log(e, "Caught in StartServer", Logger.Level.ERROR);
        }
    }

}
