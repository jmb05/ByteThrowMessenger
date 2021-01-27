package net.jmb19905.messenger.messages;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EncryptedMessage extends Message{

    private final String type;
    private final byte[][] encryptedData;

    public EncryptedMessage(String sender, String type, byte[][] encryptedData) {
        super(sender);
        this.type = type;
        this.encryptedData = encryptedData;
    }

    public String getType() {
        return type;
    }

    public byte[][] getEncryptedData() {
        return encryptedData;
    }

    @Override
    public EncryptedMessage toEncrypted() {
        return this;
    }

    @Override
    public String toString() {
        StringBuilder dataString = new StringBuilder();
        for(byte[] dataPart : encryptedData){
            dataString.append(new String(dataPart));
            dataString.append("<|>");
        }
        return sender + "<|>" + type + "<|>" + dataString;
    }

    public static EncryptedMessage fromString(String s) {
        String sender;
        String type;
        byte[][] data;

        String[] parts = s.split("<\\|>");
        sender = parts[0];
        type = parts[1];

        int maxLength = 0;
        List<byte[]> dataAsList = new ArrayList<>();
        for(int i = 2;i < parts.length;i++){
            byte[] dataPart = parts[i].getBytes(StandardCharsets.UTF_8);
            maxLength = Math.max(dataPart.length, maxLength);
            dataAsList.add(dataPart);
        }

        data = new byte[maxLength][0];
        for(int i = 0;i < dataAsList.size();i++){
            data[i] = dataAsList.get(i);
        }

        return new EncryptedMessage(sender, type, data);
    }

}
