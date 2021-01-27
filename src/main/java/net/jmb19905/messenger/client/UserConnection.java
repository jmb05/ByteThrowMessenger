package net.jmb19905.messenger.client;

import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.messages.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The Connection between two Users from the clients POV
 */
public class UserConnection {

    private final String name;
    private EncryptedConnection encryptedConnection;
    private final List<Message> messages;

    public UserConnection() {
        this("", null);
    }

    public UserConnection(String name, EncryptedConnection encryptedConnection) {
        this.name = name;
        this.encryptedConnection = encryptedConnection;
        this.messages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public EncryptedConnection getEncryptedConnection() {
        return encryptedConnection;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public <T extends Message> void addMessages(List<T> messages){
        this.messages.addAll(messages);
    }

    public void clear(){
        this.messages.clear();
    }

    public void setEncryptedConnection(EncryptedConnection encryptedConnection) {
        this.encryptedConnection = encryptedConnection;
    }

    public void close(){
        File connectionFile = new File("userdata/" + ByteThrowClient.getUsername() + "/" + name + ".json");
        if(connectionFile.exists()){
            connectionFile.delete();
        }
        //TODO: delete media if wanted
    }

    @Override
    public String toString() {
        return "UserConnection{" +
                "name='" + name + '\'' +
                ", encryptedConnection=" + encryptedConnection +
                ", messages=" + messages +
                '}';
    }
}
