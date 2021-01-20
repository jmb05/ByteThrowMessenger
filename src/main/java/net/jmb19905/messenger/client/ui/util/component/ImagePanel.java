package net.jmb19905.messenger.client.ui.util.component;

import net.jmb19905.messenger.util.ImageUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A Panel that displays an image
 */
public class ImagePanel extends JPanel{

    private final BufferedImage image;
    private final int displaySizeX;
    private final int displaySizeY;
    private final Dimension size;

    public ImagePanel(BufferedImage image, int displaySizeX, int displaySizeY){
        this.image = image;
        this.displaySizeX = displaySizeX;
        this.displaySizeY = displaySizeY;

        size = new Dimension(displaySizeX, displaySizeY);
        setPreferredSize(size);
        setSize(size);
        setMaximumSize(size);
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponents(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setColor(Color.white);
        graphics2D.fillRect(0,0, size.width, size.height);
        graphics2D.drawImage(ImageUtility.resizeImage(image, displaySizeX, displaySizeY), 0, 0, this);
    }
}