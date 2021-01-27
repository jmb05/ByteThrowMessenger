package net.jmb19905.messenger.server;

import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.packets.FailPacket;
import net.jmb19905.messenger.packets.SuccessPacket;
import net.jmb19905.messenger.packets.ToClientDataPacket;
import net.jmb19905.messenger.util.EncryptionUtility;

import java.util.Collection;

public class ServerUtils {

    //Success Packets
    public static SuccessPacket createRegisterSuccessPacket(){
        SuccessPacket successMessage = new SuccessPacket();
        successMessage.type = "register";
        return successMessage;
    }

    public static SuccessPacket createLoginSuccessPacket(){
        SuccessPacket successMessage = new SuccessPacket();
        successMessage.type = "login";
        return successMessage;
    }


    //Fail Packets
    public static FailPacket createLoginNameFailPacket(){
        FailPacket fail = new FailPacket();
        fail.type = "loginFail";
        fail.cause = "name";
        return fail;
    }

    public static FailPacket createLoginPasswordFailPacket(){
        FailPacket fail = new FailPacket();
        fail.type = "loginFail";
        fail.cause = "pw";
        return fail;
    }

    public static FailPacket createRegisterNameTakenPacket(){
        FailPacket fail = new FailPacket();
        fail.type = "usernameTaken";
        return fail;
    }

    public static FailPacket createInternalRegisterFailPacket(){
        FailPacket fail = new FailPacket();
        fail.type = "registerFail";
        fail.cause = "There was an internal database error";
        return fail;
    }

    public static FailPacket createClientOutOfDatePacket(){
        FailPacket fail = new FailPacket();
        fail.type = "outOfDate";
        fail.cause = ByteThrowServer.version;
        return fail;
    }

    public static FailPacket createUserConnectionErrorPacket(String otherUser){
        FailPacket fail = new FailPacket();
        fail.type = "connectFail";
        fail.cause = otherUser;
        return fail;
    }

    //Data Packet
    public static ToClientDataPacket<Message> createHistoryPacket(EncryptedConnection connection, E2EConnection e2EConnection, String otherUser){
        ToClientDataPacket<Message> dataPacket = new ToClientDataPacket<>();
        dataPacket.otherUser = EncryptionUtility.encryptString(connection, otherUser);
        dataPacket.type = EncryptionUtility.encryptString(connection, "chatHistory");
        dataPacket.data.addAll(e2EConnection.getHistory());
        return dataPacket;
    }

}
