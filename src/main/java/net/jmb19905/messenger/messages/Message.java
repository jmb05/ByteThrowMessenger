package net.jmb19905.messenger.messages;

public abstract class Message {

    public String sender;

    public Message(String sender){
        this.sender = sender;
    }

}
