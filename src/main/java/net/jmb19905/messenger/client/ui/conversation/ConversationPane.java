package net.jmb19905.messenger.client.ui.conversation;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EXPERIMENTAL: Pane with Messages on it
 */
public class ConversationPane extends JDesktopPane {

    private List<JInternalFrame> frames = new ArrayList<>();

    public ConversationPane() {
        MessageFrame frame = new MessageFrame("Thiiiiiiiiiiis iiiiiiiiiiiis a veeeeeeeeeeery looooooooooooooooooooooooong text", 100, -1, this);
    }

    @Override
    public void repaint() {

        super.repaint();
    }
}
