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
