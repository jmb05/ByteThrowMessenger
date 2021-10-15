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
import net.jmb19905.bytethrow.common.packets.ChatsPacket;

import java.util.ArrayList;
import java.util.List;

public class ClientGroupChat extends GroupChat implements IClientChat<GroupMessage> {

    private List<GroupMessage> messages = new ArrayList<>();

    public ClientGroupChat(ChatsPacket.ChatData data){
        this(data.name());
        this.members = data.members();
        this.uniqueId = data.id();
    }

    public ClientGroupChat(String name) {
        super(name);
    }

    public void addMessage(GroupMessage message){
        messages.add(message);
    }

    @Override
    public void merge(IClientChat<GroupMessage> other) {
        if(other == null) return;
        ClientGroupChat groupChat = (ClientGroupChat) other;
        if(!groupChat.getName().equals(getName())) return;
        members = groupChat.members;
        messages.addAll(other.getMessages());
        List<GroupMessage> newMessages = new ArrayList<>();
        messages.stream().sorted().forEach(newMessages::add);
        messages = newMessages;
    }

    public void removeMessage(GroupMessage message){
        messages.remove(message);
    }

    @Override
    public List<GroupMessage> getMessages() {
        return messages;
    }
}
