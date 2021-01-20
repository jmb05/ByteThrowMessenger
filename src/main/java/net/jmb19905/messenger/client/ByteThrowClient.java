package net.jmb19905.messenger.client;

import com.esotericsoftware.minlog.Log;
import net.jmb19905.messenger.client.ui.settings.SettingsWindow;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.util.*;
import net.jmb19905.messenger.util.config.ConfigManager;
import net.jmb19905.messenger.util.logging.BTMLogger;

import java.io.*;

public class ByteThrowClient {

    public static String version;

    private static String username = "";
    private static String password = "";

    private static boolean loggedIn = false;

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
        startUp();
        if (clientConfig.autoLogin) {
            readUserData();
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
        Variables.currentSide = "client";
        BTMLogger.setLevel(BTMLogger.LEVEL_TRACE);
        Log.set(Log.LEVEL_DEBUG);
        BTMLogger.init();
        version = Util.readVersion();
        clientConfig = ConfigManager.loadClientConfigFile("config/client_config.json");
        SettingsWindow.setLookAndFeel(clientConfig.theme);
        Variables.initFonts();
    }

    /**
     * Reads the user data from "/userdata/user.dat"
     */
    private static void readUserData() {
        try {
            File file = new File("userdata/user.dat");
            if (FileUtility.createFile(file)) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                try {
                    if (!reader.readLine().equals("UserData:")) {
                        return;
                    }
                    username = reader.readLine();
                    password = reader.readLine();
                    reader.close();
                } catch (NullPointerException e) {
                    BTMLogger.warn("MessagingClient", "No UserData found in file user.dat - login required");
                }
            }else {
                BTMLogger.warn("MessagingClient", "Error creating userdata file");
            }
        } catch (IOException e) {
            BTMLogger.warn("MessagingClient", "Error reading userdata", e);
        }
    }

    public static void setUserData(String username, String password) {
        ByteThrowClient.username = username;
        ByteThrowClient.password = password;
    }

    public static void setLoggedIn(boolean loggedIn) {
        ByteThrowClient.loggedIn = loggedIn;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Writes the userdata to "/userdata/user.dat"
     */
    public static void writeUserData() {
        if (!username.equals("") && !password.equals("")) {
            try {
                File file = new File("userdata/user.dat");
                if(FileUtility.createFile(file)) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write("UserData:\n");
                    writer.write(username + "\n");
                    writer.write(password + "\n");
                    writer.close();
                }else{
                    BTMLogger.warn("MessagingClient", "Error creating userdata file");
                }
            } catch (IOException e) {
                BTMLogger.info("MessagingClient", "Error writing userdata", e);
            }
        } else {
            BTMLogger.warn("MessagingClient", "Can't write UserData to file 'user.dat'! Incomplete data");
        }
    }

    /**
     * Deletes the userdata
     */
    public static void wipeUserData() {
        username = "";
        password = "";
        try {
            File userDat = new File("userdata/user.dat");
            if (userDat.exists()) {
                userDat.delete();
                userDat.createNewFile();
            }
        } catch (IOException e) {
            BTMLogger.warn("MessagingClient", "Cannot wipe userdata (user.dat)");
        }
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }
}
