package net.jmb19905.messenger.messages;

import java.nio.charset.StandardCharsets;

public class TextMessage extends Message{

    public String text;

    /**
     * Used By Jackson
     */
    public TextMessage(){
        this("", "");
    }

    public TextMessage(String sender, String text) {
        super(sender);
        this.text = text;
    }

    @Override
    public EncryptedMessage toEncrypted() {
        String type = "text";
        byte[][] data = new byte[text.length()][1];
        data[0] = text.getBytes(StandardCharsets.UTF_8);
        return new EncryptedMessage(sender, type, data);
    }

    @Override
    public String toString() {
        return "text:" + sender + ":" + text;
    }

    public static TextMessage fromString(String s) {
        String[] parts = s.split(":");
        if(parts[0].equals("text")){
            String sender = parts[1];
            StringBuilder builder = new StringBuilder();
            for(int i=2;i<parts.length;i++){
                builder.append(parts[i]);
            }
            return new TextMessage(sender, builder.toString());
        }
        return new TextMessage("", "");
    }
}
