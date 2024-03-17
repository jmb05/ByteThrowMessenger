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

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.GroupChat;
import net.jmb19905.bytethrow.common.chat.IChat;
import net.jmb19905.jmbnetty.common.buffer.BufferSerializable;
import net.jmb19905.jmbnetty.common.buffer.SimpleBuffer;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.util.*;

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
    public void construct(SimpleBuffer buffer) {
        update = buffer.getBoolean();
        chatData.clear();

        int length = buffer.getInt();
        for (int i=0;i<length;i++) {
            ChatData data = buffer.get(ChatData.class);
            chatData.add(data);
        }
    }

    @Override
    public void deconstruct(SimpleBuffer buffer) {
        buffer.putBoolean(update);
        buffer.putInt(chatData.size());
        for (ChatData data : chatData) {
            buffer.put(data);
        }
    }

    public static final class ChatData implements BufferSerializable {
        private String name;
        private final List<User> members;
        private UUID id;

        public ChatData() {
            this.members = new ArrayList<>();
        }

        public ChatData(String name, List<User> members, UUID id) {
            this.name = name;
            this.members = members;
            this.id = id;
        }

        public ChatData(IChat chat) {
                this(chat instanceof GroupChat ? ((GroupChat) chat).getName() : null, chat.getMembers(), chat.getUniqueId());
            }

            @Override
            public void construct(SimpleBuffer buffer) {
                name = buffer.getString();
                User[] array = buffer.getArray(User.class);
                members.clear();
                members.addAll(Arrays.stream(array).toList());
                id = buffer.getUUID();
            }

            @Override
            public void deconstruct(SimpleBuffer buffer) {
                buffer.putString(name);
                buffer.putArray(members.toArray(new User[0]));
                buffer.putUUID(id);
            }

        public String name() {
            return name;
        }

        public List<User> members() {
            return members;
        }

        public UUID id() {
            return id;
        }
    }

}
