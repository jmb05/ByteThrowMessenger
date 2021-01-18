package net.jmb19905.messenger.server;

import com.esotericsoftware.minlog.Log;
import net.jmb19905.messenger.util.ConfigManager;
import net.jmb19905.messenger.util.BTMLogger;
import net.jmb19905.messenger.util.Util;
import net.jmb19905.messenger.util.Variables;

public class ByteThrowServer {

    public static String version;

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
     * Initializes Variable, BTMLogger, Log, ServerConfig
     */
    private static void startUp() {
        Variables.currentSide = "server";
        BTMLogger.setLevel(BTMLogger.LEVEL_TRACE);
        Log.set(Log.LEVEL_DEBUG);
        BTMLogger.init();
        version = Util.readVersion();
        config = ConfigManager.loadServerConfigFile("config/server_config.json");
    }

}
