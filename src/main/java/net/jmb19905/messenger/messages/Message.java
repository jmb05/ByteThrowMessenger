package net.jmb19905.messenger.messages;

public abstract class Message {

    public String recipient;

    public Message(String recipient){
        this.recipient = recipient;
    }

}
