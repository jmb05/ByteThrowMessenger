package net.jmb19905.messenger.client;

import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.packages.*;
import net.jmb19905.messenger.util.EncryptionUtility;
import net.jmb19905.messenger.util.FileUtility;
import net.jmb19905.messenger.util.FormattedImage;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

public class ClientUtils {

    public static DataPacket createDataPacket(String username, String message, EncryptedConnection clientToServerConnection, EncryptedConnection endToEndConnection){
        DataPacket dataPacket = new DataPacket();
        dataPacket.username = EncryptionUtility.encryptString(clientToServerConnection, username);
        dataPacket.type = EncryptionUtility.encryptString(clientToServerConnection, EncryptionUtility.encryptString(endToEndConnection, "text"));
        dataPacket.data = new byte[][] {clientToServerConnection.encrypt(endToEndConnection.encrypt(message.getBytes(StandardCharsets.UTF_8)))};
        return dataPacket;
    }

    public static DataPacket createDataPacket(String username, String caption, FormattedImage[] images, EncryptedConnection clientToServerConnection, EncryptedConnection endToEndConnection, StringBuilder imagePathsBuilder){
        DataPacket dataPacket = new DataPacket();
        dataPacket.username = EncryptionUtility.encryptString(clientToServerConnection, username);
        dataPacket.type = EncryptionUtility.encryptString(clientToServerConnection, EncryptionUtility.encryptString(endToEndConnection, "image"));
        byte[][] data = new byte[(images.length * 2) + 1][0];
        byte[] rawCaption = caption.getBytes(StandardCharsets.UTF_8);
        data[0] = rawCaption;
        for(int i=0;i<images.length;i++){
            FormattedImage image = images[i];
            byte[] meta = (image.name + "|" + image.format + "|" + image.image.getWidth() + "|" + image.image.getHeight()).getBytes(StandardCharsets.UTF_8);
            data[i * 2 + 1] = meta;
            byte[] imageData = FileUtility.convertImageToBytes(image.image);
            data[i * 2 + 2] = imageData;
            imagePathsBuilder.append("userdata/").append(ByteThrowClient.getUsername()).append("/media/").append(image.toString()).append("|");
        }
        dataPacket.data = EncryptionUtility.decrypt2DBytes(clientToServerConnection, EncryptionUtility.encrypt2DBytes(endToEndConnection, data));
        return dataPacket;
    }

    public static LoginPacket createLoginPacket(String username, String password, EncryptedConnection clientToServerConnection){
        LoginPacket loginPacket = new LoginPacket();
        loginPacket.username = EncryptionUtility.encryptString(clientToServerConnection, username);
        loginPacket.password = EncryptionUtility.encryptString(clientToServerConnection, password);
        return loginPacket;
    }

    public static RegisterPacket createRegisterPacket(String username, String password, EncryptedConnection clientToServerConnection){
        RegisterPacket registerPacket = new RegisterPacket();
        registerPacket.username = EncryptionUtility.encryptString(clientToServerConnection, username);
        registerPacket.password = EncryptionUtility.encryptString(clientToServerConnection, password);
        return registerPacket;
    }

    public static StartEndToEndConnectionPacket createStartEndToEndConnectionPacket(String username, EncryptedConnection clientToServerConnection, EncryptedConnection endToEndConnection){
        StartEndToEndConnectionPacket connectPacket = new StartEndToEndConnectionPacket();
        connectPacket.username = EncryptionUtility.encryptString(clientToServerConnection, username);
        connectPacket.publicKeyEncodedEncrypted = clientToServerConnection.encrypt(endToEndConnection.getPublicKey().getEncoded());
        return connectPacket;
    }

    public static PublicKeyPacket createPubicKeyPacket(String appVersion, EncryptedConnection clientToServerConnection){
        PublicKeyPacket publicKeyPacket = new PublicKeyPacket();
        publicKeyPacket.version = appVersion;
        publicKeyPacket.encodedKey = clientToServerConnection.getPublicKey().getEncoded();
        return publicKeyPacket;
    }

}
