package net.jmb19905.client.gui.components;

import javax.swing.*;
import java.awt.*;

/**
 * A Panel that shows an Image either by painting it on using Graphics or a GIF using the AnimatedIconLabel
 */
public class PicturePanel extends JPanel {

    /**
     * The static image (is null if GIF is shown)
     */
    private Image image;

    /**
     * The GIF (is null if the static image is shown)
     */
    private ImageIcon gif;

    private PicturePanel(){
        setLayout(new GridBagLayout());
    }

    public PicturePanel(Image image) {
        this();
        this.image = image;
    }

    public PicturePanel(ImageIcon gif){
        this();
        this.gif = gif;
        AnimatedIconLabel label = new AnimatedIconLabel(gif);
        add(label);
    }

    public void setTransparent(boolean transparent) {
        setOpaque(!transparent);
    }

    public boolean isTransparent() {
        return !isOpaque();
    }

    /**
     * Paints static the image onto Panel and sets the PreferredSize to the size of the image
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image != null) {
            g.drawImage(image, 0, 0, this);
            setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
        }else {
            setPreferredSize(new Dimension(gif.getImage().getWidth(this), gif.getImage().getHeight(this)));
        }
        if(isTransparent()){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0,0, getWidth(), getHeight());
        }
        revalidate();
    }
}