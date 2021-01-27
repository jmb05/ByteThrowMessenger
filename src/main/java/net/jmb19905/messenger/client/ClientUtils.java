package net.jmb19905.messenger.client;

import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.packets.*;
import net.jmb19905.messenger.util.EncryptionUtility;
import net.jmb19905.messenger.util.FileUtility;
import net.jmb19905.messenger.util.FormattedImage;
import net.jmb19905.messenger.util.ImageUtility;

import java.nio.charset.StandardCharsets;

public class ClientUtils {

    public static DataPacket createDataPacket(String username, String message, EncryptedConnection clientToServerConnection, EncryptedConnection endToEndConnection){
        DataPacket dataPacket = new DataPacket();
        dataPacket.username = EncryptionUtility.encryptString(clientToServerConnection, username);
        dataPacket.type = EncryptionUtility.encryptString(clientToServerConnection, EncryptionUtility.encryptString(endToEndConnection, "text"));
        dataPacket.data = new byte[][] {clientToServerConnection.encrypt(endToEndConnection.encrypt(message.getBytes(StandardCharsets.UTF_8)))};
        return dataPacket;
    }

    public static DataPacket createDataPacket(String username, String caption, FormattedImage[] images, EncryptedConnection clientToServerConnection, EncryptedConnection endToEndConnection){
        DataPacket dataPacket = new DataPacket();
        dataPacket.username = EncryptionUtility.encryptString(clientToServerConnection, username);
        dataPacket.type = EncryptionUtility.encryptString(clientToServerConnection, EncryptionUtility.encryptString(endToEndConnection, "image"));
        byte[][] data = ImageUtility.imagesToBytes(caption, images);
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

    public static E2EInfoPacket createCloseConnectionPacket(String username, EncryptedConnection recipientConnection){
        E2EInfoPacket infoPacket = new E2EInfoPacket();
        infoPacket.username = EncryptionUtility.encryptString(MessagingClient.serverConnection, username);
        infoPacket.type = EncryptionUtility.encryptString(MessagingClient.serverConnection, "close");
        return infoPacket;
    }

    public static ToServerDataRequestPacket createHistoryRequest(EncryptedConnection connection, String otherUser){
        ToServerDataRequestPacket dataRequestPacket = new ToServerDataRequestPacket();
        dataRequestPacket.type = EncryptionUtility.encryptString(connection, "chatHistory");
        dataRequestPacket.data = EncryptionUtility.encryptString(connection, otherUser);
        return dataRequestPacket;
    }

}
