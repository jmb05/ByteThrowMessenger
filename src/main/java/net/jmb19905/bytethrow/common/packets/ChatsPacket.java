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

package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.bytethrow.common.chat.Chat;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatsPacket extends Packet {

    private static final String ID = "chats";

    public boolean update = false;
    public List<ChatData> chatData = new ArrayList<>();

    /**
     * Contains all the names of the peers of a client
     */
    public ChatsPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        update = Boolean.parseBoolean(data[1]);

        String[] parts = Arrays.copyOfRange(data, 2, data.length);
        Arrays.stream(parts).forEach(s -> chatData.add(ChatData.fromString(s)));
    }

    @Override
    public byte[] deconstruct() {
        StringBuilder namesBuilder = new StringBuilder();
        for (ChatData chatData : chatData) {
            if(chatData != null) {
                namesBuilder.append("|").append(chatData);
            }
        }
        return (ID + "|" + update + namesBuilder).getBytes(StandardCharsets.UTF_8);
    }

    public static record ChatData(String name, List<String> members) {
        public ChatData(Chat chat) {
            this(chat instanceof GroupChat ? ((GroupChat) chat).getName() : null, chat.getMembers());
        }

        public static ChatData fromString(String s){
            String[] parts = s.split(",");
            String name = parts[0];
            String[] members = parts[1].replace("(", "").replace(")", "").split("\\\\");
            return new ChatData(name, new ArrayList<>(List.of(members)));
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            members.forEach(s -> builder.append(s).append("\\"));
            return name + ",(" + builder + ")";
        }
    }

}
