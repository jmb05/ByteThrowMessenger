package net.jmb19905.messenger.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.ChatHistory;
import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

public class FileUtility {

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
            BTMLogger.error(FileUtility.class.getName(), "Error writing ChatHistory to File", e);
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
                BTMLogger.error(FileUtility.class.getName(), "Error reading ChatHistory from File", e);
            }
        } else {
            BTMLogger.warn(FileUtility.class.getName(), "Cannot read ChatHistory from File - ChatHistory does not exist");
        }
        return null;
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
            BTMLogger.warn(FileUtility.class.getName(), "Error loading image");
            return null;
        }
    }

    /**
     * Gets a Resource as Stream
     * @param s path for the resource
     * @return the stream
     */
    public static InputStream getResource(String s) {
        return FileUtility.class.getClassLoader().getResourceAsStream(s);
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
            BTMLogger.warn(FileUtility.class.getName(), "Error creating file: " + file.getAbsolutePath());
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
            BTMLogger.warn(FileUtility.class.getName(), "Error converting Image to bytes", e);
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
            BTMLogger.warn(FileUtility.class.getName(), "Error converting bytes to Image", e);
            return null;
        }
    }


}
