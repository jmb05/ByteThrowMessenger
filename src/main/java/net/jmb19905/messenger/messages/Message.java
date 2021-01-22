package net.jmb19905.messenger.messages;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

@JsonSerialize(using = Message.JsonSerializer.class)
@JsonDeserialize(using = Message.JsonDeserializer.class)
public abstract class Message {

    public String sender;

    public Message(String sender){
        this.sender = sender;
    }

    public static class JsonSerializer extends StdSerializer<Message>{

        public JsonSerializer(){this(null);}

        public JsonSerializer(Class<Message> t) {
            super(t);
        }

        @Override
        public void serialize(Message value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value instanceof TextMessage){
                gen.writeStartObject();
                gen.writeStringField("sender", value.sender);
                gen.writeStringField("text", ((TextMessage) value).text);
                gen.writeEndObject();
            }
        }
    }

    public static class JsonDeserializer extends StdDeserializer<Message>{

        public JsonDeserializer() {
            this(null);
        }

        public JsonDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Message deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode jsonNode = p.getCodec().readTree(p);
            String sender = jsonNode.get("sender").asText();
            String text = jsonNode.get("text").asText();
            try {
                String image = jsonNode.get("image").asText();
            }catch (NullPointerException ignored){/*Only if the message is an ImageMessage we need this*/}
            return new TextMessage(sender, text);
        }
    }

}
