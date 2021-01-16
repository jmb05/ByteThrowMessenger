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
        addInternalFrame("Thid is a very loooooooooooooooooooooooong text", 100, 100);
    }

    private void addInternalFrame(String text, int xPos, int yPos){
        JInternalFrame internalFrame = new JInternalFrame();

        internalFrame.setLayout(new GridBagLayout());
        internalFrame.setLocation(xPos, yPos);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8,8,8,8);

        internalFrame.add(new JLabel(text), constraints);
        frames.add(internalFrame);

        internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                System.out.println("Activated internalframe");
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                System.out.println("Deactivated internalframe");
            }
        });

        internalFrame.pack();
        internalFrame.setVisible(true);
        add(internalFrame);
    }

    @Override
    public void repaint() {

        super.repaint();
    }
}
