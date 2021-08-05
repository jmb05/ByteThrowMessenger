package net.jmb19905.client.gui.components;

import javax.swing.*;
import java.awt.*;

public class PicturePanel extends JPanel {

    private Image image;
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