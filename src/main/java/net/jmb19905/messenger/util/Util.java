package net.jmb19905.messenger.util;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

public class Util {

    private static final SystemTray systemTray;

    //only create SystemTray if on client - server is not needed and if it has no gui it will likely crash
    static {
        if (Variables.currentSide.equals("client")) {
            systemTray = SystemTray.getSystemTray();
        } else {
            systemTray = null;
        }
    }

    /**
     * Decodes a PublicKey from a byte-array
     * @param encodedKey the key encoded as byte-array
     * @return the decoded PublicKey
     * @throws InvalidKeySpecException when the encoded key parameter is invalid
     */
    public static PublicKey createPublicKeyFromData(byte[] encodedKey) throws InvalidKeySpecException {
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException e) {
            EMLogger.warn("Util", "Error retrieving PublicKey", e);
            return null;
        }
    }

    /**
     * Decodes a PrivateKey from a byte-array
     * @param encodedKey the key encoded as byte-array
     * @return the decoded PublicKey
     * @throws InvalidKeySpecException when the encoded key parameter is invalid
     */
    public static PrivateKey createPrivateKeyFromData(byte[] encodedKey) throws InvalidKeySpecException{
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            EMLogger.warn("Util", "Error retrieving PrivateKey", e);
            return null;
        }
    }

    /**
     * Loads the saved ChatHistories for the current user
     * @return a HashMap with the username of the history as key and the ChatHistory object as value
     */
    public static HashMap<String, ChatHistory> loadChatHistories() {
        HashMap<String, ChatHistory> map = new HashMap<>();
        File parentDirectory = new File("userdata/" + EncryptedMessenger.getUsername() + "/");
        if (!parentDirectory.exists() || !parentDirectory.isDirectory()) {
            parentDirectory.mkdirs();
        }
        for (File file : parentDirectory.listFiles()) {
            String username = file.getName().split("\\.")[0];
            map.put(username, loadChatHistory(username));
            EncryptedMessenger.window.addConnectedUser(username);
        }
        return map;
    }

    /**
     * Saves the ChatHistories for the current user
     * @param nodes a HashMap with the username of the history as key and the ChatHistory object as value
     */
    public static void saveChatHistories(HashMap<String, ChatHistory> nodes) {
        for (String name : nodes.keySet()) {
            saveChatHistory(name, nodes.get(name));
        }
    }

    /**
     * Saves a single ChatHistory for the current user
     * @param username the username of the history
     * @param chat the ChatHistory
     */
    public static void saveChatHistory(String username, ChatHistory chat) {
        File file = new File("userdata/" + EncryptedMessenger.getUsername() + "/" + username + ".json");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, chat);
        } catch (IOException e) {
            EMLogger.error("Util", "Error writing ChatHistory to File", e);
        }
    }

    /**
     * Loads a single ChatHistory for the current user
     * @param username the username of the history
     * @return the loaded ChatHistory
     */
    public static ChatHistory loadChatHistory(String username) {
        File file = new File("userdata/" + EncryptedMessenger.getUsername() + "/" + username + ".json");
        if (file.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(file, ChatHistory.class);
            } catch (IOException e) {
                EMLogger.error("Util", "Error reading ChatHistory from File", e);
            }
        } else {
            EMLogger.warn("Util", "Cannot read ChatHistory from File - ChatHistory does not exist");
        }
        return null;
    }

    /**
     * Registers the ALL the Messages
     * @param kryo the Kryo that the messages will be registered to
     */
    public static void registerMessages(Kryo kryo) {
        kryo.register(LoginPublicKeyMessage.class);
        kryo.register(byte[].class);
        kryo.register(LoginMessage.class);
        kryo.register(RegisterMessage.class);
        kryo.register(SuccessMessage.class);
        kryo.register(ConnectWithOtherUserMessage.class);
        kryo.register(DataMessage.class);
        kryo.register(FailMessage.class);
    }

    /**
     * Encrypts a String in the UTF-8 encoding
     * @param node the Node that will encrypt the String
     * @param value the String to be encrypted
     * @return the encrypted String
     */
    public static String encryptString(Node node, String value) {
        return new String(node.encrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Decrypts a String in the UTF-8 encoding
     * @param node the Node that will decrypt the String
     * @param value the String to be decrypted
     * @return the decrypted String
     */
    public static String decryptString(Node node, String value) {
        return new String(node.decrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Loads an image from the classpath
     * @param s the path of the image
     * @return the loaded image
     */
    public static Image getImageResource(String s) {
        try {
            InputStream stream = getResource(s);
            return ImageIO.read(stream);
        } catch (IOException e) {
            EMLogger.warn("Util", "Error loading image");
            return null;
        }
    }

    /**
     * Gets a Resource as Stream
     * @param s path for the resource
     * @return the stream
     */
    public static InputStream getResource(String s) {
        return Util.class.getClassLoader().getResourceAsStream(s);
    }

    /**
     * Displays a System Notification
     * @param title the title of the Notification
     * @param text the content of the Notification
     * @param icon the icon of the Notification
     */
    public static void displayNotification(String title, String text, Image icon) {
        TrayIcon trayIcon = new TrayIcon(icon, "EM Notification");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("EM Notification");

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        trayIcon.displayMessage(title, text, TrayIcon.MessageType.NONE);

    }

    /**
     * Creates a file
     * @param fileName the path of the File that will be created
     * @return if the file was created or already existed
     */
    public static boolean createFile(String fileName){
        return createFile(new File(fileName));
    }

    /**
     * Creates a  file
     * @param file the File that will be created
     * @return if the file was created or already existed
     */
    public static boolean createFile(File file){
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                return file.createNewFile();
            }else{
                return true;
            }
        } catch (IOException e) {
            EMLogger.warn("Util", "Error creating file: " + file.getAbsolutePath());
        }
        return false;
    }

}
