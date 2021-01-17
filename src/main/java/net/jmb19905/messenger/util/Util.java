package net.jmb19905.messenger.util;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
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

    private static final SystemTray systemTray = SystemTray.getSystemTray();

    public static PublicKey createPublicKeyFromData(byte[] encodedKey) throws InvalidKeySpecException {
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException e) {
            EMLogger.error("Util", "Error retrieving PublicKey", e);
            return null;
        }
    }

    public static PrivateKey createPrivateKeyFromData(byte[] encodedKey){
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            EMLogger.error("Util", "Error retrieving PrivateKey", e);
            return null;
        }
    }

    public static HashMap<String, ChatHistory> loadNodes(){
        HashMap<String, ChatHistory> map = new HashMap<>();
        File parentDirectory = new File("userdata/" + EncryptedMessenger.getUsername() + "/");
        if(parentDirectory.exists() && parentDirectory.isDirectory()){
            for(File file : parentDirectory.listFiles()){
                String username = file.getName().split("\\.")[0];
                map.put(username, readNode(username));
                EncryptedMessenger.window.addConnectedUser(username);
            }
        }
        return map;
    }

    public static void saveNodes(HashMap<String, ChatHistory> nodes){
        for(String name : nodes.keySet()){
            saveNode(name, nodes.get(name));
        }
    }

    public static void saveNode(String username, ChatHistory chat){
        File file = new File("userdata/" + EncryptedMessenger.getUsername() + "/" + username + ".json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, chat);
        } catch (IOException e) {
            EMLogger.error("Util", "Error writing ChatHistory to File", e);
        }
    }

    public static ChatHistory readNode(String username){
        File file = new File("userdata/" + EncryptedMessenger.getUsername() + "/" + username + ".json");
        if(file.exists()){
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(file, ChatHistory.class);
            } catch (IOException e) {
                EMLogger.error("Util", "Error reading ChatHistory from File", e);
            }
        }else{
            EMLogger.warn("Util", "Cannot read ChatHistory from File - ChatHistory does not exist");
        }
        return null;
    }

    public static void registerMessages(Kryo kryo){
        kryo.register(LoginPublicKeyMessage.class);
        kryo.register(byte[].class);
        kryo.register(LoginMessage.class);
        kryo.register(RegisterMessage.class);
        kryo.register(UsernameAlreadyExistMessage.class);
        kryo.register(RegisterSuccessfulMessage.class);
        kryo.register(NotRegisteredMessage.class);
        kryo.register(LoginSuccessMessage.class);
        kryo.register(ConnectWithOtherUserMessage.class);
        kryo.register(DataMessage.class);
        kryo.register(LoginFailedMessage.class);
    }

    public static String encryptString(Node node, String value){
        return new String(node.encrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decryptString(Node node, String value){
        return new String(node.decrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static Image getImageResource(String s){
        try {
            InputStream stream = getResource(s);
            System.out.println(stream);
            return ImageIO.read(stream);
        } catch (IOException e) {
            EMLogger.warn("Util", "Error loading image");
            return null;
        }
    }
    public static InputStream getResource(String s){
        return Util.class.getClassLoader().getResourceAsStream(s);
    }

    public static void displayNotification(String title, String text, Image icon){
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

}
