package net.jmb19905.messenger.messages;

public class TextMessage extends Message{

    public String text;

    public TextMessage(String recipient, String text) {
        super(recipient);
        this.text = text;
    }

}
