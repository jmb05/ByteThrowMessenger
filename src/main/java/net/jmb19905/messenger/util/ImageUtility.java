package net.jmb19905.messenger.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtility {

    /**
     * Resizes a BufferedImage
     * @param image the Input Image
     * @param x the new x Size
     * @param y the new y Size
     * @return the resized Image
     */
    public static BufferedImage resizeImage(BufferedImage image, int x, int y){
        Image tmp = image.getScaledInstance(x, y, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    /**
     * Resizes a BufferedImage uniformly using the width
     * @param image the Image that will be resized
     * @param width the width
     * @return a resized instance of the image
     */
    public static BufferedImage resizeImageUniformlyX(BufferedImage image, int width){
        int height = image.getHeight() / image.getWidth() * width;
        return resizeImage(image, width, height);
    }

    /**
     * Resizes a BufferedImage uniformly using the height
     * @param image the Image that will be resized
     * @param height the width
     * @return a resized instance of the image
     */
    public static BufferedImage resizeImageUniformlyY(BufferedImage image, int height){
        int width = image.getWidth() / image.getHeight() * height;
        return resizeImage(image, width, height);
    }

}
