package net.jmb19905.client.gui.components;

import net.jmb19905.common.util.ResourceUtility;

import javax.swing.*;

/**
 * Shows a gif on a JLabel
 */
public class AnimatedIconLabel extends JLabel {

    /**
     * The Gif as Icon
     */
    private final Icon imgIcon;

    public AnimatedIconLabel(String resource){
        imgIcon = new ImageIcon(ResourceUtility.getResourceAsURL(resource));
        setIcon(imgIcon);
    }

    public AnimatedIconLabel(Icon icon){
        this.imgIcon = icon;
        setIcon(imgIcon);
    }

    public Icon getIcon() {
        return imgIcon;
    }
}
