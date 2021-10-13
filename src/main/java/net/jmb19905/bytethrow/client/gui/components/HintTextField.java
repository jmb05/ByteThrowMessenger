/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.client.gui.components;

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
            g.setFont(new Font("Narwhal", Font.PLAIN, 17));
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