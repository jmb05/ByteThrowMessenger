package net.jmb19905.messenger.client.ui.util.component;

import net.jmb19905.messenger.util.Variables;

import javax.swing.*;
import java.awt.*;

//From Stackoverflow: https://stackoverflow.com/questions/1738966/java-jtextfield-with-input-hint
public class HintTextField extends JTextField {

    private final String hint;

    public HintTextField(String hint) {
        this.hint = hint;
    }

    /**
     * This draws a string over the JTextField but only if there is no text in the field
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().length() == 0) {
            int h = getHeight();
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Insets ins = getInsets();
            g.setFont(Variables.defaultFont);
            FontMetrics fm = g.getFontMetrics();
            int backgroundColor = getBackground().getRGB();
            int foregroundColor = getForeground().getRGB();
            int m = 0xfefefefe;
            int hintTextColor = ((backgroundColor & m) >>> 1) + ((foregroundColor & m) >>> 1);
            g.setColor(new Color(hintTextColor, true));
            g.drawString(hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
        }
    }
}