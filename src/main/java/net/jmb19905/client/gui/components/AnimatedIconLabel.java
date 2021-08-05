package net.jmb19905.client.gui.components;

import net.jmb19905.client.ResourceUtility;

import javax.swing.*;

public class AnimatedIconLabel extends JLabel {

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
