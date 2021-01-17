package net.jmb19905.messenger.server;

import com.esotericsoftware.minlog.Log;
import net.jmb19905.messenger.util.ConfigManager;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Variables;

public class ServerMain {

    public static ConfigManager.ServerConfig config;

    public static MessagingServer messagingServer;

    /**
     * Starts the Server
     * @param args the Program arguments
     */
    public static void main(String[] args) {
        startUp();
        messagingServer = new MessagingServer();
        messagingServer.start();
    }

    /**
     * Initializes Variable, EMLogger, Log, ServerConfig
     */
    private static void startUp() {
        Variables.currentSide = "server";
        EMLogger.setLevel(EMLogger.LEVEL_DEBUG);
        Log.set(Log.LEVEL_INFO);
        EMLogger.init();
        config = ConfigManager.loadServerConfigFile("config/server_config.json");
    }

}
