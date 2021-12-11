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

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.AbstractChat;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.util.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatSerial {

    public static List<AbstractChat> readAllChats() {
        List<AbstractChat> chats = new ArrayList<>();
        try {
            Path chatsDirectory = Paths.get("chats/");
            if(!Files.exists(chatsDirectory)) Files.createDirectories(chatsDirectory);
            Files.list(chatsDirectory).forEach(path -> {
                AbstractChat chat = read(UUID.fromString(path.getFileName().toString()));
                if(chat != null) {
                    chats.add(chat);
                }
            });
        } catch (IOException e) {
            Logger.error(e);
        }
        Logger.debug("Loaded Chats: " + chats);
        return chats;
    }

    public static AbstractChat read(UUID uuid) {
        Path chatFilePath = Paths.get("chats/" + uuid.toString());
        if (Files.exists(chatFilePath)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(chatFilePath.toFile()))) {
                String name = reader.readLine();
                List<User> users = new ArrayList<>();
                for(String userData : reader.readLine().split(",")) {
                    users.add(User.constructUser(userData));
                }

                AbstractChat chat;

                if (!name.equals("null")) {
                    chat = new GroupChat(name, uuid);
                    chat.setMembers(users);
                } else {
                    chat = new PeerChat(users.get(0), users.get(1), uuid);
                }

                return chat;
            } catch (IOException e) {
                Logger.error(e);
            } catch (NullPointerException e){
                Logger.warn("Invalid chat file found -> deleting");
                deleteChatFile(uuid);
            }
        }
        return null;
    }

    public static void write(AbstractChat chat) {
        Path chatFilePath = Paths.get("chats/" + chat.getUniqueId().toString());
        try {
            if (!Files.exists(chatFilePath)) {
                Files.createDirectories(chatFilePath.getParent());
                Files.createFile(chatFilePath);
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(chatFilePath.toFile()))) {
                writer.write(chat instanceof PeerChat ? "null" : ((GroupChat) chat).getName());
                writer.newLine();
                StringBuilder members = new StringBuilder();
                chat.getMembers().forEach(member -> members.append(member.toSafeString()).append(","));
                writer.write(members.toString());
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.debug("Wrote Chat: " + chat.getUniqueId());
    }

    public static void deleteChatFile(UUID uuid){
        Path chatFilePath = Paths.get("chats/" + uuid.toString());
        try {
            Files.delete(chatFilePath);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public static void deleteChatFile(AbstractChat chat) {
        deleteChatFile(chat.getUniqueId());
    }
}
