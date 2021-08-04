package net.jmb19905.client;

import net.jmb19905.common.util.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ResourceUtility {

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

}
