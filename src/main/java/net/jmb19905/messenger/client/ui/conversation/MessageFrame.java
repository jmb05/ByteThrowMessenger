package net.jmb19905.messenger.client.ui.conversation;

import javax.swing.*;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.util.ArrayList;
import java.util.List;

/**
 * EXPERIMENTAL: The Messages for the ConversationPane
 */
public class MessageFrame extends JInternalFrame {

    public static final int LEFT = -1;
    public static final int RIGHT = 1;

    public String text;
    private int yPos;
    private final int alignment;

    private JTextArea textArea;

    private List<String> lines = new ArrayList<>();

    public MessageFrame(String text, int yPos, int alignment, JComponent parent) {
        parent.add(this);
        this.text = text;
        this.textArea = new JTextArea();
        this.yPos = yPos;
        this.alignment = alignment;


        lines.add(text);

        textArea.setEditable(false);

        if (alignment > 0) {
            setLocation(getRightSeparation() + 20, yPos);
        } else {
            setLocation(20, yPos);
        }
    }

    private int getLeftSeparation() {
        return getParent().getWidth() / 14 * 5;
    }

    private int getRightSeparation() {
        return getParent().getWidth() / 14 * 9;
    }

    @Override
    public void repaint() {
        if (alignment > 0) {
            setLocation(getRightSeparation() + 20, yPos);
        } else {
            //setLocation(20, yPos);
            if (getWidth() + 20 > getLeftSeparation() - 20) {
                List<String> newLines = new ArrayList<>();
                for (String line : lines) {
                    int preciseMiddle = line.length() / 2;
                    int newMiddle;
                    char c;
                    int pointer = 0;
                    while (true) {
                        newMiddle = preciseMiddle + pointer;
                        c = line.charAt(newMiddle);
                        if (c == ' ') {
                            break;
                        }
                        newMiddle = preciseMiddle - pointer;
                        c = line.charAt(newMiddle);
                        if (c == ' ') {
                            break;
                        }
                        pointer++;
                    }
                    newLines.add(line.substring(0, newMiddle - 1));
                    newLines.add(line.substring(newMiddle));
                }
                lines = newLines;
            }
        }
        for (String line : lines) {
            textArea.append(line + "\n");
        }
        super.repaint();
    }

    //Removes the title bar
    @Override
    public void setUI(InternalFrameUI ui) {
        super.setUI(ui);
        BasicInternalFrameUI frameUI = (BasicInternalFrameUI) getUI();
        if (frameUI != null) frameUI.setNorthPane(null);
    }

}
