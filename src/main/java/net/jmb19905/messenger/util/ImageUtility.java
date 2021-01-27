package net.jmb19905.messenger.util;

import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    public static BufferedImage cropImage(BufferedImage src, int x, int y, int w, int h){
        return src.getSubimage(x, y, w, h);
    }

    public static BufferedImage clearSection(BufferedImage src, int x, int y, int w, int h){
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dst.createGraphics();
        Color transparent = new Color(0, 0, 0, 0);
        g2d.drawImage(src, 0, 0, dst.getWidth(), dst.getHeight(), transparent, null);
        g2d.setBackground(transparent);
        g2d.clearRect(x, y, w, h);
        g2d.dispose();
        return dst;
    }

    public static BufferedImage cropToCircle(BufferedImage src, int xMid, int yMid, int radius){
        BufferedImage cropped = cropImage(src, xMid - radius, yMid - radius, radius * 2, radius * 2);
        BufferedImage dst = new BufferedImage(cropped.getWidth(), cropped.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dst.createGraphics();
        Color transparent = new Color(0, 0, 0, 0);
        g2d.drawImage(src, 0, 0, dst.getWidth(), dst.getHeight(), transparent, null);
        g2d.setBackground(transparent);
        for(int x=0;x<src.getWidth();x++){
            for(int y=0;y<src.getHeight();y++){
                if(Math.pow((x - xMid), 2) + Math.pow((y - yMid), 2) > radius * radius){
                    g2d.clearRect(x, y, 1, 1);
                }
            }
            System.out.println("Column: " + x);
        }
        g2d.dispose();
        return dst;
    }

    public static byte[][] imagesToBytes(String caption, FormattedImage... images) {
        byte[][] data = new byte[1][(images.length * 2) + 1];
        data[0] = caption.getBytes(StandardCharsets.UTF_8);
        for(int i=0;i<images.length;i++){
            FormattedImage image = images[i];
            byte[] meta = (image.name + "|" + image.format + "|" + image.image.getWidth() + "|" + image.image.getHeight()).getBytes(StandardCharsets.UTF_8);
            data[i * 2 + 1] = meta;
            byte[] imageData = FileUtility.convertImageToBytes(image.image);
            data[i * 2 + 2] = imageData;
        }
        return data;
    }

}
