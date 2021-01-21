package net.jmb19905.messenger.messages;

import net.jmb19905.messenger.util.FormattedImage;

public class ImageMessage extends Message{

    public FormattedImage[] images;
    public String caption;

    /**
     * Used By Jackson
     */
    public ImageMessage(){
        this("", "");
    }

    public ImageMessage(String sender, String caption, FormattedImage... images) {
        super(sender);
        this.images = images;
        this.caption = caption;
    }



}
