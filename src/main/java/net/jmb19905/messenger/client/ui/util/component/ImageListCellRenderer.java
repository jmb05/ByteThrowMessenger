package net.jmb19905.messenger.client.ui.util.component;

import net.jmb19905.messenger.util.Variables;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ImageListCellRenderer extends DefaultListCellRenderer {

    private final Map<String, ImageIcon> imageMap;

    public ImageListCellRenderer(Map<String, ImageIcon> imageMap){
        this.imageMap = imageMap;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String username = (String) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setIcon(imageMap.get(username));
        label.setHorizontalTextPosition(JLabel.RIGHT);
        label.setFont(Variables.defaultFont);
        return label;
    }
}