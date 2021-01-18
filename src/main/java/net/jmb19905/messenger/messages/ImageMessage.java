package net.jmb19905.messenger.messages;

import java.awt.*;

public class ImageMessage extends Message{

    public Image image;
    public String caption;

    public ImageMessage(String recipient, Image image, String caption) {
        super(recipient);
        this.image = image;
        this.caption = caption;
    }


}
