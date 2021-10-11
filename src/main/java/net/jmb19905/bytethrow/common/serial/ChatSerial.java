/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.serial;

import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.util.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatSerial {

    public static List<Chat> readAllChats(){
        List<Chat> chats = new ArrayList<>();
        try {
            Path chatsDirectory = Paths.get("chats/");
            Files.list(chatsDirectory).forEach(path -> chats.add(read(UUID.fromString(path.getFileName().toString()))));
        } catch (IOException e) {
            Logger.error(e);
        }
        return chats;
    }

    public static Chat read(UUID uuid) {
        Path chatFilePath = Paths.get("chats/" + uuid.toString());
        if(Files.exists(chatFilePath)) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(chatFilePath.toFile()))) {
                return (Chat) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Logger.error(e);
            }
        }
        return null;
    }

    public static void write(Chat chat) {
        Path chatFilePath = Paths.get("chats/" + chat.getUniqueId().toString());
        try {
            if(!Files.exists(chatFilePath)){
                Files.createDirectories(chatFilePath.getParent());
                Files.createFile(chatFilePath);
            }
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(chatFilePath.toFile()))) {
                outputStream.writeObject(chat);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
