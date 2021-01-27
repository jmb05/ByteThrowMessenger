package net.jmb19905.messenger.messages;

import net.jmb19905.messenger.util.FormattedImage;
import net.jmb19905.messenger.util.ImageUtility;

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


    @Override
    public EncryptedMessage toEncrypted() {
        byte[][] data = ImageUtility.imagesToBytes(caption, images);
        return new EncryptedMessage(sender, "image", data);
    }

    @Override
    public String toString() {
        return null; //TODO: implement
    }

    public static ImageMessage fromString(String s) {
        return null; //TODO: implement
    }
}
