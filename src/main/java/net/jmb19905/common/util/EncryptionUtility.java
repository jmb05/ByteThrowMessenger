package net.jmb19905.common.util;

import net.jmb19905.common.crypto.EncryptedConnection;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Utility methods used for Encryption
 */
public class EncryptionUtility {

    /**
     * Decodes a PublicKey from a byte-array
     * @param encodedKey the key encoded as byte-array
     * @return the decoded PublicKey
     * @throws InvalidKeySpecException when the encoded key parameter is invalid
     */
    public static PublicKey createPublicKeyFromData(byte[] encodedKey) throws InvalidKeySpecException {
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException e) {
            Logger.log(e, "Error retrieving PublicKey", Logger.Level.WARN);
            return null;
        }
    }

    /**
     * Decodes a PrivateKey from a byte-array
     * @param encodedKey the key encoded as byte-array
     * @return the decoded PublicKey
     * @throws InvalidKeySpecException when the encoded key parameter is invalid
     */
    public static PrivateKey createPrivateKeyFromData(byte[] encodedKey) throws InvalidKeySpecException{
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            Logger.log(e, "Error retrieving PrivateKey", Logger.Level.WARN);
            return null;
        }
    }

    /**
     * Encrypts a String in the UTF-8 encoding
     * @param encryptedConnection the EncryptedConnection that will encrypt the String
     * @param value the String to be encrypted
     * @return the encrypted String
     */
    public static String encryptString(EncryptedConnection encryptedConnection, String value) {
        return new String(encryptedConnection.encrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Decrypts a String in the UTF-8 encoding
     * @param encryptedConnection the EncryptedConnection that will decrypt the String
     * @param value the String to be decrypted
     * @return the decrypted String
     */
    public static String decryptString(EncryptedConnection encryptedConnection, String value) {
        return new String(encryptedConnection.decrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Encrypts a 2-Dimensional array of bytes
     * @param encryptedConnection the EncryptedConnection that will be used for encryption
     * @param data the 2D byte-array
     * @return an encrypted 2D byte-array
     */
    public static byte[][] encrypt2DBytes(EncryptedConnection encryptedConnection, byte[][] data){
        for(int i=0;i<data.length;i++){
            data[i] = encryptedConnection.encrypt(data[i]);
        }
        return data;
    }

    /**
     * Decrypts a 2-Dimensional array of bytes
     * @param encryptedConnection the EncryptedConnection that will be used for decryption
     * @param data the 2D byte-array
     * @return an decrypted 2D byte-array
     */
    public static byte[][] decrypt2DBytes(EncryptedConnection encryptedConnection, byte[][] data){
        for(int i=0;i<data.length;i++){
            data[i] = encryptedConnection.decrypt(data[i]);
        }
        return data;
    }

}
