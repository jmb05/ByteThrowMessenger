package net.jmb19905.messenger.crypto;

import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class Node {

    private PublicKey publickey;
    private PrivateKey privateKey;
    KeyAgreement keyAgreement;
    byte[] sharedSecret;

    private static final String ALGO = "AES";

    public Node(byte[] encodedPublicKey, byte[] encodedPrivateKey, byte[] sharedSecret){
        publickey = Util.createPublicKeyFromData(encodedPublicKey);
        privateKey = Util.createPrivateKeyFromData(encodedPrivateKey);
        this.sharedSecret = sharedSecret;
        try {
            keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            EMLogger.error("Crypto", "Error initializing Node", e);
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
            System.exit(-1);
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
}