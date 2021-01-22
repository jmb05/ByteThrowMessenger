package net.jmb19905.messenger.client.ui.conversation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationPane extends JDesktopPane {

    private final List<MessageFrameWrapper> messages = new ArrayList<>();

    public static final int LEFT = -1;
    public static final int CENTER = 0;
    public static final int RIGHT = 1;

    public ConversationPane(int width, int height){
        setSize(new Dimension(width, height));
    }

    public void addMessage(MessageFrame messageFrame, Color color, int alignment){
        int yPos = 20;
        try {
            for(MessageFrameWrapper frame : messages) {
                yPos += (frame.messageFrame.getHeight() + 5);
            }
        }catch (ArrayIndexOutOfBoundsException ignored){}
        if(alignment == LEFT){
            messageFrame.setLocation(20, yPos);
        }else if(alignment == CENTER){
            messageFrame.setLocation((getWidth() / 2) - (messageFrame.getWidth() / 2), yPos);
        }else if(alignment == RIGHT){
            messageFrame.setLocation(getWidth() - messageFrame.getWidth() - 20, yPos);
        }
        messageFrame.setColor(color);
        add(messageFrame);
        messageFrame.setVisible(true);
        messageFrame.show();
        setPreferredSize(new Dimension(getPreferredSize().width, messageFrame.getLocation().y + messageFrame.getHeight() + 10));
        messages.add(new MessageFrameWrapper(messageFrame, color, alignment));
    }

    public void clear(){
        removeAll();
    }

    @Override
    public void repaint() {
        try {
            removeAll();
            List<MessageFrameWrapper> oldList = new ArrayList<>(messages);
            messages.clear();
            for (MessageFrameWrapper message : oldList) {
                addMessage(message.messageFrame, message.color, message.alignment);
                message.messageFrame.pack();
            }
        }catch (NullPointerException ignored){}
        super.repaint();
    }

    private static class MessageFrameWrapper{

        public final MessageFrame messageFrame;
        public Color color;
        public int alignment;

        public MessageFrameWrapper(MessageFrame messageFrame, Color color, int alignment) {
            this.messageFrame = messageFrame;
            this.color = color;
            this.alignment = alignment;
        }
    }

}
