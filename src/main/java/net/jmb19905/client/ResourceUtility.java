package net.jmb19905.client;

import net.jmb19905.common.util.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class ResourceUtility {

    public static Properties readResourceProperties(String s){
        Properties prop = new Properties();
        try {
            InputStream stream = getResource(s);
            prop.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * Loads an image from the classpath
     * @param s the path of the image
     * @return the loaded image
     */
    public static BufferedImage getImageResource(String s) {
        try {
            InputStream stream = getResource(s);
            return ImageIO.read(stream);
        } catch (IOException e) {
            Logger.log("Error loading image", Logger.Level.WARN);
            return null;
        }
    }

    /**
     * Gets a Resource as a Stream
     * @param s path for the resource
     * @return the stream
     */
    public static InputStream getResource(String s) {
        return ResourceUtility.class.getClassLoader().getResourceAsStream(s);
    }

    public static URL getResourceAsURL(String s){
        return ResourceUtility.class.getClassLoader().getResource(s);
    }

}
