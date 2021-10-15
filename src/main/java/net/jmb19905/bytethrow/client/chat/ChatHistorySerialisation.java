/*
 * A simple Messenger written in Java
 * Copyright (C) 2020-2021  Jared M. Bennett
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.jmb19905.bytethrow.client.chat;

import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.chat.GroupMessage;
import net.jmb19905.bytethrow.common.chat.Message;
import net.jmb19905.bytethrow.common.chat.PeerMessage;
import net.jmb19905.util.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ChatHistorySerialisation {

    public static void saveChats(String username, List<IClientChat<? extends Message>> chats){
        chats.forEach(chat -> saveChat(username, chat));
    }

    public static List<IClientChat<? extends Message>> readAllChats(String username){
        List<IClientChat<? extends Message>> chats = new ArrayList<>();
        Path chatsDir = Paths.get("chatHistories/" + username + "/");
        if(Files.exists(chatsDir)){
            try {
                Files.list(chatsDir).forEach(path -> chats.add(readChat(username, UUID.fromString(path.getFileName().toString()))));
            } catch (IOException e) {
                Logger.error(e);
            }
        }
        return chats;
    }

    public static void saveChat(String username, IClientChat<? extends Message> chat) {
        try {
            Path path = Paths.get("chatHistories/" + username + "/" + chat.getUniqueId());
            if(!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
                if (chat instanceof ClientGroupChat) {
                    writer.write(((ClientGroupChat) chat).getName());
                } else {
                    writer.write("null");
                }
                writer.newLine();
                StringBuilder membersBuilder = new StringBuilder();
                chat.getMembers().forEach(member -> membersBuilder.append(member).append(","));
                writer.write(membersBuilder.toString());
                writer.newLine();
                for (Message message : chat.getMessages()) {
                    writer.write(message.deconstruct());
                    writer.newLine();
                }
                writer.flush();
            }
        }catch (IOException e) {
            Logger.error(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <M extends Message> IClientChat<M> readChat(String username, UUID id){
        Path path = Paths.get("chatHistories/" + username +"/" + id);
        if(Files.exists(path)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                IClientChat<M> chat;
                String name = reader.readLine();
                String members = reader.readLine();
                String[] membersPart = members.split(",");
                Stream<String> messages = reader.lines();
                if(name.equals("null")){
                    chat = (IClientChat<M>) new ClientPeerChat(membersPart[0], membersPart[1]);
                    messages.forEach(message -> {
                        M peerMessage = (M) PeerMessage.construct(message);
                        chat.addMessage(peerMessage);
                    });
                }else {
                    chat = (IClientChat<M>) new ClientGroupChat(name);
                    ((GroupChat) chat).addClients(new ArrayList<>(List.of(membersPart)));
                    messages.forEach(message -> {
                        M peerMessage = (M) GroupMessage.construct(message);
                        chat.addMessage(peerMessage);
                    });
                }
                return chat;
            } catch (IOException e) {
                Logger.error(e);
            }
        }
        return null;
    }

    public static <M extends Message> void deleteHistory(String username, IClientChat<M> chat){
        Path path = Paths.get("chatHistories/" + username + "/" + chat.getUniqueId());
        if(!Files.exists(path)) return;
        try {
            Files.delete(path);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

}
