package net.jmb19905.messenger.server;

import com.esotericsoftware.minlog.Log;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Variables;

public class ServerMain {

    public static MessagingServer messagingServer;

    public static void main(String[] args) {
        startUp();
        messagingServer = new MessagingServer();
        messagingServer.start();
    }

    private static void startUp(){
        Variables.currentSide = "server";
        EMLogger.setLevel(EMLogger.LEVEL_DEBUG);
        Log.set(Log.LEVEL_DEBUG);
        EMLogger.init();
    }

}
