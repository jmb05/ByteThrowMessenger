package net.jmb19905.messenger.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jmb19905.messenger.messages.EncryptedMessage;
import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.util.IFileName;
import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * The Connection between two Client from the Servers POV
 * The server will hold the encrypted History of the Client connections so that the client can access them if on a new device of if he lost it
 */
@JsonSerialize(using = E2EConnection.JsonSerializer.class)
@JsonDeserialize(using = E2EConnection.JsonDeserializer.class)
public class E2EConnection implements IFileName {

    private List<EncryptedMessage> history;
    private String username1;
    private String username2;

    public E2EConnection(){}

    public E2EConnection(String username1, String username2){
        this.username1 = username1;
        this.username2 = username2;
        this.history = new ArrayList<>();
    }

    public void addAllMessages(Collection<EncryptedMessage> messages){
        for(EncryptedMessage message : messages){
            addMessage(message);
        }
    }

    public void addMessage(EncryptedMessage message){
        if(message.sender.equals(username1) || message.sender.equals(username2)){
            history.add(message);
        }else{
            BTMLogger.warn("MessagingServer", "Cannot add to Messages History: invalid usename");
        }
    }

    public void removeMessage(EncryptedMessage message){
        history.remove(message);
    }

    public void close(){
        history = null;
        username1 = null;
        username2 = null;
    }

    public String getUsername1() {
        return username1;
    }

    public String getUsername2() {
        return username2;
    }

    public List<EncryptedMessage> getHistory() {
        return history;
    }

    @Nullable
    public static E2EConnection getByNames(List<E2EConnection> connections, String name1, String name2){
        for(E2EConnection connection : connections){
            if((connection.username1.equals(name1) && connection.username2.equals(name2)) || (connection.username1.equals(name2) && connection.username2.equals(name1))){
                return connection;
            }
        }
        return null;
    }

    public boolean namesMatch(E2EConnection connection){
        if(this.equals(connection)) return true;
        return (getUsername1().equals(connection.getUsername1()) && getUsername2().equals(connection.getUsername2())) || (getUsername2().equals(connection.getUsername1()) && getUsername1().equals(connection.getUsername2()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        E2EConnection that = (E2EConnection) o;
        return Objects.equals(history, that.history) && ((Objects.equals(username1, that.username1) && Objects.equals(username2, that.username2)) || (Objects.equals(username1, that.username2) && Objects.equals(username2, that.username1)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(history, username1, username2);
    }

    @Override
    public String getFileName() {
        return getUsername1() + "-" + getUsername2();
    }

    public static class JsonSerializer extends StdSerializer<E2EConnection>{

        public JsonSerializer(){this(null);}

        public JsonSerializer(Class<E2EConnection> t) {
            super(t);
        }

        @Override
        public void serialize(E2EConnection value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name1", value.username1);
            gen.writeStringField("name3", value.username2);
            gen.writeArrayFieldStart("history");
            for(Message message : value.history){
                gen.writeString(message.toString());
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }

    public static class JsonDeserializer extends StdDeserializer<E2EConnection>{

        public JsonDeserializer(){this(null);}

        public JsonDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public E2EConnection deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            String name1 = node.get("name1").asText();
            String name2 = node.get("name2").asText();
            ArrayNode historyNode = (ArrayNode) node.get("history");
            List<EncryptedMessage> messages = new ArrayList<>();
            for(int i = 0;i < historyNode.size();i++){
                messages.add(EncryptedMessage.fromString(historyNode.get(i).asText()));
            }
            E2EConnection connection = new E2EConnection(name1, name2);
            connection.addAllMessages(messages);
            return connection;
        }
    }

}
