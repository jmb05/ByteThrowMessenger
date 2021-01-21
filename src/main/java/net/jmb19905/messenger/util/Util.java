package net.jmb19905.messenger.util;

import com.esotericsoftware.kryo.Kryo;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.packets.*;
import net.jmb19905.messenger.util.logging.BTMLogger;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All the Utility methods that didn't really fit into the other Utility classes
 *
 * @see FileUtility
 * @see ImageUtility
 * @see EncryptionUtility
 */
public class Util {

    private static final SystemTray systemTray;

    //only create SystemTray if on client and if it is supported - server does not need it and could crash
    static {
        if (Variables.currentSide.equals("client") && SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();
        } else {
            systemTray = null;
        }
    }

    /**
     * Registers the ALL the Packages
     * @param kryo the Kryo that the Packages will be registered to
     *
     * @see Kryo
     * @see com.esotericsoftware.kryonet.Server
     * @see com.esotericsoftware.kryonet.Client
     */
    public static void registerPackages(Kryo kryo) {
        kryo.register(PublicKeyPacket.class);
        kryo.register(byte[].class);
        kryo.register(byte[][].class);
        kryo.register(LoginPacket.class);
        kryo.register(RegisterPacket.class);
        kryo.register(StartEndToEndConnectionPacket.class);
        kryo.register(DataPacket.class);
        kryo.register(SuccessPacket.class);
        kryo.register(FailPacket.class);
        kryo.register(E2EInfoPacket.class);
        kryo.register(KeepAlivePacket.class);
    }

    /**
     * Displays a System Notification
     * @param title the title of the Notification
     * @param text the content of the Notification
     * @param icon the icon of the Notification
     *
     * @see SystemTray
     * @see TrayIcon
     */
    public static void displayNotification(String title, String text, Image icon) {
        if(systemTray != null) {
            TrayIcon trayIcon = new TrayIcon(icon, "BTM Notification");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("BTM Notification");

            try {
                systemTray.add(trayIcon);
            } catch (AWTException e) {
                BTMLogger.warn("Util", "Error adding Icon to Notification");
            }
            trayIcon.displayMessage(title, text, TrayIcon.MessageType.NONE);
        }
    }

    /**
     * Read a string (format: [45, 34, -32, ...]) and returns the bytes
     * @param arrayAsString the String
     * @return the bytes
     */
    public static byte[] readByteArray(String arrayAsString){
        if(!arrayAsString.startsWith("[") || !arrayAsString.endsWith("]")){
            return new byte[0];
        }
        String[] parts = arrayAsString.replaceAll("\\[", "").replaceAll("]", "").split(", ");
        byte[] output = new byte[parts.length];
        for(int i=0;i<parts.length;i++){
            try {
                output[i] = (byte) Integer.parseInt(parts[i]);
            }catch (NumberFormatException e){
                BTMLogger.warn("Util", "String array does not represent a byte array", e);
                return new byte[0];
            }
        }
        return output;
    }

    /**
     * Loads the version of the program from the Resources
     * @return the version as as string
     */
    public static String readVersion(){
        BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtility.getResource("version.txt")));
        String version = null;
        try {
            version = reader.readLine();
            reader.close();
        } catch (IOException e) {
            BTMLogger.error("Util", "Error loading version");
            if(ByteThrowClient.messagingClient != null){
                ByteThrowClient.messagingClient.stop(-1);
            }
            System.exit(-1);
        }
        return version;
    }

    /**
     * Changes the Font of a Component and all Components that are inside it
     * @param component the Component
     * @param font the new Font
     *
     * @see Font
     * @see Component
     */
    public static void changeFont(Component component, Font font) {
        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                changeFont (child, font);
            }
        }
        component.repaint();
    }

    /**
     * Checks if the provided String is at least 8 characters long, contains at least one Upper and one Lowercase letter, at least one digit and at least one symbol
     * @param password the provided Password as String
     * @return if the password is valid
     */
    public static boolean checkPasswordRules(String password){
        if(password.length() < 8){
            return false;
        }
        Pattern pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        boolean symbolFlag = matcher.find();
        if(!symbolFlag){
            return false;
        }
        char currentChar;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        for(int i=0;i < password.length();i++) {
            currentChar = password.charAt(i);
            if( Character.isDigit(currentChar)) {
                numberFlag = true;
            }else if (Character.isUpperCase(currentChar)) {
                capitalFlag = true;
            }else if (Character.isLowerCase(currentChar)) {
                lowerCaseFlag = true;
            }
            if(numberFlag && capitalFlag && lowerCaseFlag) {
                return true;
            }
        }
        return false;
    }

}
