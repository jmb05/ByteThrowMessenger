package net.jmb19905.messenger.util;

import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.UserConnection;
import net.jmb19905.messenger.client.seralisation.ChatSerializer;
import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

public class FileUtility {

    /**
     * Loads the saved ChatHistories for the current user
     * @return a HashMap with the username of the history as key and the UserConnection object as value
     */
    public static HashMap<String, UserConnection> loadUserConnections(){
        HashMap<String, UserConnection> map = new HashMap<>();
        File parentDirectory = new File("userdata/" + ByteThrowClient.getUsername() + "/");
        if (!parentDirectory.exists() || !parentDirectory.isDirectory()) {
            parentDirectory.mkdirs();
        }
        for (File file : parentDirectory.listFiles()) {
            if(file.isDirectory()) {
                ChatSerializer serializer = new ChatSerializer(file.getName());
                try {
                    map.put(serializer.getName(), serializer.deserializeUserConnection());
                }catch (FileNotFoundException e){
                    BTMLogger.warn("ChatSerializer", "Error deserializing data... File not found. Fetching data from server", e);
                }catch (IOException e){
                    BTMLogger.warn("ChatSerializer", "Error deserializing data... Error reading. Fetching data from server", e);
                }
            }
        }
        return map;
    }

    /**
     * Saves the ChatHistories for the current user
     * @param userConnectionHashMap a HashMap with the username of the history as key and the UserConnection object as value
     */
    public static void saveUserConnections(HashMap<String, UserConnection> userConnectionHashMap) {
        if(userConnectionHashMap != null) {
            for (String name : userConnectionHashMap.keySet()) {
                ChatSerializer serializer = new ChatSerializer(name);
                serializer.serializeUserConnection(userConnectionHashMap.get(name));
            }
        }
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
