package net.jmb19905.messenger.util;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.packages.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
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
            BTMLogger.warn("Util", "Error retrieving PublicKey", e);
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
            BTMLogger.warn("Util", "Error retrieving PrivateKey", e);
            return null;
        }
    }

    /**
     * Loads the saved ChatHistories for the current user
     * @return a HashMap with the username of the history as key and the ChatHistory object as value
     */
    public static HashMap<String, ChatHistory> loadChatHistories() {
        HashMap<String, ChatHistory> map = new HashMap<>();
        File parentDirectory = new File("userdata/" + ByteThrowClient.getUsername() + "/");
        if (!parentDirectory.exists() || !parentDirectory.isDirectory()) {
            parentDirectory.mkdirs();
        }
        for (File file : parentDirectory.listFiles()) {
            String username = file.getName().split("\\.")[0];
            map.put(username, loadChatHistory(username));
            ByteThrowClient.window.addConnectedUser(username);
        }
        return map;
    }

    /**
     * Saves the ChatHistories for the current user
     * @param chatHistoryHashMap a HashMap with the username of the history as key and the ChatHistory object as value
     */
    public static void saveChatHistories(HashMap<String, ChatHistory> chatHistoryHashMap) {
        if(chatHistoryHashMap != null) {
            for (String name : chatHistoryHashMap.keySet()) {
                saveChatHistory(name, chatHistoryHashMap.get(name));
            }
        }
    }

    /**
     * Saves a single ChatHistory for the current user
     * @param username the username of the history
     * @param chat the ChatHistory
     */
    public static void saveChatHistory(String username, ChatHistory chat) {
        File file = new File("userdata/" + ByteThrowClient.getUsername() + "/" + username + ".json");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, chat);
        } catch (IOException e) {
            BTMLogger.error("Util", "Error writing ChatHistory to File", e);
        }
    }

    /**
     * Loads a single ChatHistory for the current user
     * @param username the username of the history
     * @return the loaded ChatHistory
     */
    public static ChatHistory loadChatHistory(String username) {
        File file = new File("userdata/" + ByteThrowClient.getUsername() + "/" + username + ".json");
        if (file.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(file, ChatHistory.class);
            } catch (IOException e) {
                BTMLogger.error("Util", "Error reading ChatHistory from File", e);
            }
        } else {
            BTMLogger.warn("Util", "Cannot read ChatHistory from File - ChatHistory does not exist");
        }
        return null;
    }

    /**
     * Registers the ALL the Packages
     * @param kryo the Kryo that the Packages will be registered to
     */
    public static void registerPackages(Kryo kryo) {
        kryo.register(LoginPublicKeyPackage.class);
        kryo.register(byte[].class);
        kryo.register(LoginPackage.class);
        kryo.register(RegisterPackage.class);
        kryo.register(ConnectWithOtherUserPackage.class);
        kryo.register(DataPackage.class);
        kryo.register(SuccessPackage.class);
        kryo.register(FailPackage.class);
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
            BTMLogger.warn("Util", "Error loading image");
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
            BTMLogger.warn("Util", "Error creating file: " + file.getAbsolutePath());
        }
        return false;
    }

    /**
     * Takes BufferedImage and returns all its bytes
     * @param image the image
     * @return the bytes
     */
    public static byte[] convertImageToBytes(BufferedImage image){
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream(65536);
            ImageIO.write(image, "png", stream);
            stream.flush();
            byte[] bytes = stream.toByteArray();
            stream.close();
            return bytes;
        } catch (IOException e) {
            BTMLogger.warn("Util", "Error converting Image to bytes", e);
            return new byte[0];
        }
    }

    /**
     * Takes bytes and turns them into a BufferedImage
     * @param bytes the bytes
     * @return the image
     */
    public static BufferedImage convertBytesToImage(byte[] bytes){
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            BTMLogger.warn("Util", "Error converting bytes to Image", e);
            return null;
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(getResource("version.txt")));
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

}
