/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.jmbnetty.common.crypto;

import net.jmb19905.util.Logger;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * An Encryption holds the Public and Private keys for a certain Connection (Server - Client | Client - Client)
 * Each side of the interaction has a separate Encryption that will take the PublicKey of the other side to generate the Shared Key
 */

public class Encryption {

    private PublicKey publickey;
    private PrivateKey privateKey;
    private KeyAgreement keyAgreement;
    private byte[] sharedSecret;

    private static final String ALGO = "AES";

    /**
     * This Constructor is for loading from a file
     *
     * @param encodedPublicKey  the PublicKey encoded in a byte-array
     * @param encodedPrivateKey the PrivateKey encoded in a byte-array
     * @param sharedSecret      the Shared Key encoded in a byte-array
     * @throws InvalidEncryptionException if the public or private key is invalid
     */
    public Encryption(byte[] encodedPublicKey, byte[] encodedPrivateKey, byte[] sharedSecret) throws InvalidEncryptionException {
        try {
            if (encodedPrivateKey != null && encodedPublicKey != null) {
                publickey = EncryptionUtility.createPublicKeyFromData(encodedPublicKey);
                privateKey = EncryptionUtility.createPrivateKeyFromData(encodedPrivateKey);
                this.sharedSecret = sharedSecret;
                try {
                    keyAgreement = KeyAgreement.getInstance("ECDH");
                    keyAgreement.init(privateKey);
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    Logger.log(e, "Error initializing EncryptedConnection", Logger.Level.ERROR);
                }
            } else {
                throw new InvalidEncryptionException("The Public or Private key is null");
            }
        } catch (InvalidKeySpecException | NullPointerException e) {
            throw new InvalidEncryptionException("The Public or Private key is invalid");
        }
    }

    /**
     * Creates a new Encryption with unique Private and Public Keys
     */
    public Encryption() {
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
            Logger.log(e, "Error initializing Encryption", Logger.Level.ERROR);
        }
    }

    /**
     * Generates the Shared Key from the other side's PublicKey
     *
     * @param publicKey the PublicKey of the other side
     */
    public void setReceiverPublicKey(PublicKey publicKey) {
        try {
            keyAgreement.doPhase(publicKey, true);
            sharedSecret = keyAgreement.generateSecret();
        } catch (InvalidKeyException e) {
            Logger.log(e, "Invalid Key", Logger.Level.ERROR);
        }
    }

    /**
     * Encrypts a byte-array using the Shared Key
     *
     * @param in the byte-array that will be encrypted
     * @return the encrypted byte-array
     */
    public byte[] encrypt(byte[] in) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(in);
            return Base64.getEncoder().withoutPadding().encode(encVal);
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException e) {
            Logger.log(e, "Error encrypting", Logger.Level.ERROR);
        } catch (IllegalArgumentException e) {
            Logger.log(e, "Error encrypting! Tried to encrypt without other PublicKey", Logger.Level.WARN);
        }
        return in;
    }

    /**
     * Decrypts a byte-array using the Shared Key
     *
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
            Logger.error(e, "Error decrypting");
        } catch (BadPaddingException e) {
            Logger.error(e, "Error decrypting - wrong key");
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
     * THIS KEY SHOULD NEVER BE USED OUTSIDE THIS DEVICE
     *
     * @return the PrivateKey
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }


    protected Key generateKey() {
        return new SecretKeySpec(sharedSecret, ALGO);
    }

    /**
     * THIS KEY SHOULD NEVER BE USED OUTSIDE THIS DEVICE
     *
     * @return the Shared Key
     */
    public byte[] getSharedSecret() {
        return sharedSecret;
    }

    public boolean isUsable() {
        return sharedSecret != null;
    }

    @Override
    public String toString() {
        return "Encryption{" +
                "publicKey=" + publickey +
                ", privateKey=" + privateKey +
                ", keyAgreement=" + keyAgreement +
                ", sharedSecret=" + Arrays.toString(sharedSecret) +
                '}';
    }
}