package net.jmb19905.messenger.client.ui.conversation;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationPane extends JDesktopPane {

    private List<JInternalFrame> frames = new ArrayList<>();

    public ConversationPane(){
        MessageFrame frame = new MessageFrame("Thiiiiiiiiiiis iiiiiiiiiiiis a veeeeeeeeeeery looooooooooooooooooooooooong text", 100, -1, this);
    }


    @Override
    public void repaint() {

        super.repaint();
    }
}
