package net.jmb19905.messenger.client;

import com.esotericsoftware.minlog.Log;
import net.jmb19905.messenger.client.ui.SettingsWindow;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.util.ConfigManager;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;
import net.jmb19905.messenger.util.Variables;

import java.io.*;

public class EncryptedMessenger {

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
     * Initializes Variable, EMLogger, Log, ClientConfig, LookAndFeel
     */
    private static void startUp() {
        Variables.currentSide = "client";
        EMLogger.setLevel(EMLogger.LEVEL_TRACE);
        Log.set(Log.LEVEL_DEBUG);
        EMLogger.init();
        clientConfig = ConfigManager.loadClientConfigFile("config/client_config.json");
        SettingsWindow.setLookAndFeel(clientConfig.theme);
    }

    /**
     * Reads the user data from "/userdata/user.dat"
     */
    private static void readUserData() {
        try {
            File file = new File("userdata/user.dat");
            if (Util.createFile(file)) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                try {
                    if (!reader.readLine().equals("UserData:")) {
                        return;
                    }
                    username = reader.readLine();
                    password = reader.readLine();
                    reader.close();
                } catch (NullPointerException e) {
                    EMLogger.warn("MessagingClient", "No UserData found in file user.dat - login required");
                }
            }else {
                EMLogger.warn("MessagingClient", "Error creating userdata file");
            }
        } catch (IOException e) {
            EMLogger.warn("MessagingClient", "Error reading userdata", e);
        }
    }

    public static void setUserData(String username, String password) {
        EncryptedMessenger.username = username;
        EncryptedMessenger.password = password;
    }

    public static void setLoggedIn(boolean loggedIn) {
        EncryptedMessenger.loggedIn = loggedIn;
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
                if(Util.createFile(file)) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write("UserData:\n");
                    writer.write(username + "\n");
                    writer.write(password + "\n");
                    writer.close();
                }else{
                    EMLogger.warn("MessagingClient", "Error creating userdata file");
                }
            } catch (IOException e) {
                EMLogger.info("MessagingClient", "Error writing userdata", e);
            }
        } else {
            EMLogger.warn("MessagingClient", "Can't write UserData to file 'user.dat'! Incomplete data");
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
            EMLogger.warn("MessagingClient", "Cannot wipe userdata (user.dat)");
        }
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }
}
