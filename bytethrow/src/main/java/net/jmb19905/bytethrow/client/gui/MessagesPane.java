/*
 * A simple Messenger written in Java
 * Copyright (C) 2020-2021  Jared M. Bennett
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.jmb19905.bytethrow.client.gui;

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.Message;
import net.jmb19905.util.Logger;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class MessagesPane extends JTextPane {

    private final StyledDocument document;

    private static final SimpleAttributeSet bold;
    private static final SimpleAttributeSet italic;
    private static final SimpleAttributeSet underlineBold;

    static {
        bold = new SimpleAttributeSet();
        bold.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

        italic = new SimpleAttributeSet();
        italic.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);

        underlineBold = new SimpleAttributeSet();
        underlineBold.addAttribute(StyleConstants.CharacterConstants.Underline, Boolean.TRUE);
        underlineBold.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
    }

    public MessagesPane(){
        this.document = getStyledDocument();
        setEditable(false);
    }

    public void appendImage(BufferedImage image) {
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        imageLabel.setOpaque(false);
        imageLabel.setBackground(Color.CYAN);
        setSelectionStart(getText().length());
        setSelectionEnd(getText().length());
        insertComponent(imageLabel);
    }

    public void append(String text, AttributeSet attributeSet) {
        try {
            document.insertString(document.getLength(), text, attributeSet);
        } catch (BadLocationException e) {
            Logger.error(e);
        }
        //Logger.logPart(text, Logger.Level.INFO);
    }

    public void newLine() {
        try {
            document.insertString(document.getLength(), "\n", null);
        } catch (BadLocationException e) {
            Logger.error(e);
        }
        //Logger.finishLine();
    }

    /**
     * Appends a String to the Window's TextArea and logs it
     *
     * @param line the String that is appended
     */
    public void appendLine(String line) {
        try {
            document.insertString(document.getLength(), line + "\n", null);
        } catch (BadLocationException e) {
            Logger.error(e);
        }
        Logger.info(line);
    }

    /**
     * Appends a String to the Window's TextArea and logs it with a specific Level
     *
     * @param line  the String that is appended
     * @param level the Level (Severity) of the log message
     */
    public void log(String line, Logger.Level level) {
        try {
            document.insertString(document.getLength(), line + "\n", null);
        } catch (BadLocationException e) {
            Logger.error(e);
        }
        Logger.log(line, level);
    }

    public void setContent(List<Message> messages){
        clear();
        for(Message message : messages){
            String[] displayMessageParts = message.getMessageDisplay().split("\\\\b");
            for(int i=0;i<displayMessageParts.length;i++){
                if(i % 2 == 0){
                    append(displayMessageParts[i], null);
                }else {
                    String sender = User.constructUser(displayMessageParts[i]).getUsername();
                    append(sender, underlineBold);
                }
            }
            newLine();
        }
    }

    public void clear(){
        setText("");
    }

    public static SimpleAttributeSet getBold() {
        return bold;
    }

    public static SimpleAttributeSet getItalic() {
        return italic;
    }

    public static SimpleAttributeSet getUnderlineBold() {
        return underlineBold;
    }
}
