package net.jmb19905.messenger.client;

import com.esotericsoftware.minlog.Log;
import net.jmb19905.messenger.client.ui.settings.SettingsWindow;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.util.*;
import net.jmb19905.messenger.util.config.ConfigManager;
import net.jmb19905.messenger.util.logging.BTMLogger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ByteThrowClient {

    public static String version;

    private static UserSession userSession;

    public static MessagingClient messagingClient;
    public static Window window;

    public static ConfigManager.ClientConfig clientConfig;

    public static String[] arguments;

    /**
     * Starts the Client
     * @param args the program arguments
     */
    public static void main(String[] args) {
        arguments = args;
        if (System.getProperty("btm.launch.type").equals("devenv")) {
            Variables.dataDirectory = "";
            System.out.println("Detected Dev Environment - ignoring home Directory");
        }
        startUp();
        if (clientConfig.autoLogin) {
            userSession = ClientSerializationUtils.readUserData();
        }
        window = new Window();
        messagingClient = new MessagingClient(clientConfig.server, clientConfig.port);
        messagingClient.start();
        window.setVisible(true);
    }

    /**
     * Initializes Variable, BTMLogger, Log, ClientConfig, LookAndFeel
     */
    private static void startUp() {
        //Tell the Program we are starting the client
        Variables.currentSide = "client";
        //Init Logger
        BTMLogger.setLevel(BTMLogger.LEVEL_INFO);
        Log.set(Log.LEVEL_INFO);
        BTMLogger.init();
        //Get the version
        version = Util.readVersion();
        //create data Directory
        FileUtility.createFolder(Variables.dataDirectory);
        //Load configs
        clientConfig = ConfigManager.loadClientConfigFile(Variables.dataDirectory + "config/client_config.json");
        //Set LaF
        SettingsWindow.setLookAndFeel(clientConfig.theme);
        //Initialize Fonts
        Variables.initFonts();
    }

    public static void setUserSession(String username, String password) {
        ByteThrowClient.userSession.username = username;
        ByteThrowClient.userSession.password = password;
    }

    public static void setSessionLogIn(boolean loggedIn) {
        userSession.loggedIn = loggedIn;
        if(loggedIn){
            window.setTitle(Window.APPLICATION_NAME + " - " + userSession.username);
        }
    }

    public static boolean isLoggedIn() {
        return userSession.loggedIn;
    }

    //TODO: test
    public static void restartApplication(){
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        File currentJar = null;
        try {
            currentJar = new File(ByteThrowClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if(!currentJar.getName().endsWith(".jar")) {
            return;
        }

        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static UserSession getUserSession(){
        return userSession;
    }
}
