package net.jmb19905.messenger.crypto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jmb19905.messenger.client.EncryptedMessenger;
import net.jmb19905.messenger.crypto.exception.InvalidNodeException;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

@JsonSerialize(using = Node.JsonSerializer.class)
@JsonDeserialize(using = Node.JsonDeserializer.class)
public class Node {

    private PublicKey publickey;
    private PrivateKey privateKey;
    KeyAgreement keyAgreement;
    byte[] sharedSecret;

    private static final String ALGO = "AES";

    public Node(byte[] encodedPublicKey, byte[] encodedPrivateKey, byte[] sharedSecret) throws InvalidNodeException {
        try {
            if (encodedPrivateKey != null && encodedPublicKey != null) {
                publickey = Util.createPublicKeyFromData(encodedPublicKey);
                privateKey = Util.createPrivateKeyFromData(encodedPrivateKey);
                this.sharedSecret = sharedSecret;
                try {
                    keyAgreement = KeyAgreement.getInstance("ECDH");
                    keyAgreement.init(privateKey);
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    EMLogger.error("Crypto", "Error initializing Node", e);
                }
            } else {
                throw new InvalidNodeException("The Public or Private key is null");
            }
        }catch (InvalidKeySpecException e){
            throw new InvalidNodeException("The Public or Private key is invalid");
        }
    }

    public Node() {
        makeKeyExchangeParams();
    }

    private void makeKeyExchangeParams() {
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(256);
            KeyPair kp = kpg.generateKeyPair();
            publickey = kp.getPublic();
            privateKey = kp.getPrivate();
            keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            EMLogger.error("CryptoNode", "Error initializing Node", e);
        }
    }

    public void setReceiverPublicKey(PublicKey publickey) {
        try {
            keyAgreement.doPhase(publickey, true);
            sharedSecret = keyAgreement.generateSecret();
        } catch (InvalidKeyException e) {
            EMLogger.error("CryptoNode", "Invalid Key", e);
        }
    }

    public byte[] encrypt(byte[] msg) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(msg);
            return Base64.getEncoder().encode(encVal);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException e) {
            EMLogger.error("CryptoNode", "Error encrypting", e);
            if(EncryptedMessenger.messagingClient != null){
                EncryptedMessenger.messagingClient.stop(-1);
            }else {
                System.exit(-1);
            }
        } catch (IllegalArgumentException e){
            EMLogger.warn("CryptoNode", "Error encrypting! Tried to encrypt without other PublicKey");
        }
        return msg;
    }

    public byte[] decrypt(byte[] encryptedData) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
            return c.doFinal(decodedValue);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | IllegalArgumentException e) {
            EMLogger.error("CryptoNode", "Error decrypting", e);
        }
        return encryptedData;
    }

    public PublicKey getPublicKey() {
        return publickey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    protected Key generateKey() {
        return new SecretKeySpec(sharedSecret, ALGO);
    }

    public byte[] getSharedSecret(){
        return sharedSecret;
    }

    @Override
    public String toString() {
        return "Node{" +
                "publickey=" + publickey +
                ", privateKey=" + privateKey +
                ", keyAgreement=" + keyAgreement +
                ", sharedSecret=" + Arrays.toString(sharedSecret) +
                '}';
    }

    public static class JsonSerializer extends StdSerializer<Node>{

        public JsonSerializer(){
            this(null);
        }

        public JsonSerializer(Class<Node> t) {
            super(t);
        }

        @Override
        public void serialize(Node value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeBinaryField("public", value.publickey.getEncoded());
            gen.writeBinaryField("private", value.privateKey.getEncoded());
            if(value.sharedSecret != null) {
                gen.writeBinaryField("shared", value.sharedSecret);
            }else{
                gen.writeBinaryField("shared", new byte[0]);
            }
            gen.writeEndObject();
        }
    }

    public static class JsonDeserializer extends StdDeserializer<Node>{

        public JsonDeserializer(){
            this(null);
        }

        public JsonDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Node deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            byte[] publicKey = node.get("public").binaryValue();
            byte[] privateKey = node.get("private").binaryValue();
            byte[] sharedKey = node.get("shared").binaryValue();
            try {
                return new Node(publicKey, privateKey, sharedKey);
            } catch (InvalidNodeException e) {
                EMLogger.error("MessagingClient", "Error deserializing Nodes from JSON", e);
                return null;
            }
        }
    }
}