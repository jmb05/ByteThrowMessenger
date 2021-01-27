package net.jmb19905.messenger.client.seralisation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.UserConnection;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.util.FileUtility;
import net.jmb19905.messenger.util.logging.BTMLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChatSerializer {

    private final File directory;
    private final String name;

    public ChatSerializer(String name, File directory){
        this.name = name;
        this.directory = directory;
        if(!directory.exists() || !directory.isDirectory()){
            directory.mkdirs();
        }
    }

    public void serializeUserConnection(UserConnection connection){
        try {
            serializeEncryptedConnection(connection.getEncryptedConnection());
            serializeChatHistory(connection.getMessages());
        }catch (IOException e){
            BTMLogger.warn("ChatSerializer", "Error serializing data... Next login might require fetch from server", e);
        }
    }

    public UserConnection deserializeUserConnection() throws IOException {
        UserConnection connection = new UserConnection(name, deserializeEncryptedConnection());
        connection.addMessages(deserializeChatHistory());
        return connection;
    }

    public void serializeEncryptedConnection(EncryptedConnection connection) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(directory.getAbsolutePath() + "/" + name + "/conn.json");
        FileUtility.createFile(file);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, connection);
    }

    public EncryptedConnection deserializeEncryptedConnection() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(directory.getAbsolutePath() + "/" + name + "/conn.json");
        FileUtility.createFile(file);
        return mapper.readValue(file, EncryptedConnection.class);
    }

    public void serializeChatHistory(List<Message> messagesHistory) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(directory.getAbsolutePath() + "/" + name + "/hist.json");
        FileUtility.createFile(file);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, messagesHistory);
    }

    public List<Message> deserializeChatHistory() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(directory.getAbsolutePath() + "/" + name + "/hist.json");
        FileUtility.createFile(file);
        TypeReference<List<Message>> listTypeReference = new TypeReference<List<Message>>() {};
        return mapper.readValue(new File(directory.getAbsolutePath() + "/" + name + "/hist.json"), listTypeReference);
    }

    public String getName() {
        return name;
    }
}
