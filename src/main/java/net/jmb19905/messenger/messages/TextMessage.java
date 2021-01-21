package net.jmb19905.messenger.messages;

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

}
