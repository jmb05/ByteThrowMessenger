package net.jmb19905.messenger.crypto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.crypto.exception.InvalidNodeException;
import net.jmb19905.messenger.util.BTMLogger;
import net.jmb19905.messenger.util.Util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * a Node holds the Public and Private keys for a certain Connection (Server - Client | Client - Client)
 * Each side of the interaction has a separate Node that will take the PublicKey of the other side to generate the Shared Key
 */
@JsonSerialize(using = Node.JsonSerializer.class)
@JsonDeserialize(using = Node.JsonDeserializer.class)
public class Node {

    private PublicKey publickey;
    private PrivateKey privateKey;
    KeyAgreement keyAgreement;
    byte[] sharedSecret;

    private static final String ALGO = "AES";

    /**
     * This Constructor is for loading from a file
     * @param encodedPublicKey the PublicKey encoded in a byte-array
     * @param encodedPrivateKey the PrivateKey encoded in a byte-array
     * @param sharedSecret  the Shared Key encoded in a byte-array
     * @throws InvalidNodeException if the public or private key is invalid
     */
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
                    BTMLogger.error("Crypto", "Error initializing Node", e);
                }
            } else {
                throw new InvalidNodeException("The Public or Private key is null");
            }
        } catch (InvalidKeySpecException | NullPointerException e) {
            throw new InvalidNodeException("The Public or Private key is invalid");
        }
    }

    /**
     * Creates a new Node with unique Private and Public Keys
     */
    public Node() {
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
            BTMLogger.error("CryptoNode", "Error initializing Node", e);
        }
    }

    /**
     * Generates the Shared Key from the other side's PublicKey
     * @param publicKey the PublicKey of the other side
     */
    public void setReceiverPublicKey(PublicKey publicKey) {
        try {
            keyAgreement.doPhase(publicKey, true);
            sharedSecret = keyAgreement.generateSecret();
        } catch (InvalidKeyException e) {
            BTMLogger.error("CryptoNode", "Invalid Key", e);
        }
    }

    /**
     * Encrypts a byte-array using the Shared Key
     * @param in the byte-array that will be encrypted
     * @return the encrypted byte-array
     */
    public byte[] encrypt(byte[] in) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(in);
            return Base64.getEncoder().encode(encVal);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException e) {
            BTMLogger.error("CryptoNode", "Error encrypting", e);
            if (ByteThrowClient.messagingClient != null) {
                ByteThrowClient.messagingClient.stop(-1);
            }
        } catch (IllegalArgumentException e) {
            BTMLogger.warn("CryptoNode", "Error encrypting! Tried to encrypt without other PublicKey");
        }
        return in;
    }

    /**
     * Decrypts a byte-array using the Shared Key
     * @param encryptedData the encrypted byte-array that will be decrypted
     * @return the decrypted byte-array
     */
    public byte[] decrypt(byte[] encryptedData) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
            return c.doFinal(decodedValue);
        } catch (InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | IllegalArgumentException e) {
            BTMLogger.error("CryptoNode", "Error decrypting", e);
        } catch (BadPaddingException e){
            BTMLogger.error("CryptoNode", "Error decrypting - wrong key", e);
        }
        return encryptedData;
    }

    /**
     * @return the PublicKey
     */
    public PublicKey getPublicKey() {
        return publickey;
    }

    /**
     * THIS KEY SHOULD NEVER BE USED OUTSIDE OF THIS DEVICE
     * @return the PrivateKey
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }


    protected Key generateKey() {
        return new SecretKeySpec(sharedSecret, ALGO);
    }

    /**
     * THIS KEY SHOULD NEVER BE USED OUTSIDE OF THIS DEVICE
     * @return the Shared Key
     */
    public byte[] getSharedSecret() {
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

    /**
     * The Serializer that converts Nodes into JSON
     */
    public static class JsonSerializer extends StdSerializer<Node> {

        public JsonSerializer() {
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
            if (value.sharedSecret != null) {
                gen.writeBinaryField("shared", value.sharedSecret);
            } else {
                gen.writeBinaryField("shared", new byte[0]);
            }
            gen.writeEndObject();
        }
    }

    /**
     * The Deserializer that retrieves Nodes from JSON
     */
    public static class JsonDeserializer extends StdDeserializer<Node> {

        public JsonDeserializer() {
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
                BTMLogger.error("MessagingClient", "Error deserializing Nodes from JSON", e);
                return null;
            }
        }
    }
}