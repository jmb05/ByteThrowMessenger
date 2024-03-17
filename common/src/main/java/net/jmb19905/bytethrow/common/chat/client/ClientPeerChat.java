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

package net.jmb19905.bytethrow.common.chat.client;

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.chat.PeerMessage;
import net.jmb19905.bytethrow.common.packets.ChatsPacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ClientPeerChat extends PeerChat implements IClientChat<PeerMessage> {

    private List<PeerMessage> messages = new ArrayList<>();

    public ClientPeerChat(ChatsPacket.ChatData data){
        this(data.members().get(0), data.members().get(1));
        this.uniqueId = data.id();
    }

    public ClientPeerChat(User peer1, User peer2) {
        super(peer1, peer2);
    }

    public void addMessage(PeerMessage message){
        messages.add(message);
    }

    @Override
    public void merge(IClientChat<PeerMessage> other) {
        if(other == null) return;
        ClientPeerChat peerChat = (ClientPeerChat) other;
        if(!new HashSet<>(peerChat.members).containsAll(members) || !new HashSet<>(members).containsAll(peerChat.members)) return;
        messages.addAll(other.getMessages());
        List<PeerMessage> newMessages = new ArrayList<>();
        messages.stream().sorted().forEach(newMessages::add);
        messages = newMessages;
    }

    public void removeMessage(PeerMessage message){
        messages.remove(message);
    }

    @Override
    public List<PeerMessage> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "ClientPeerChat{" +
                "members=" + members +
                ", messages=" + messages +
                ", encryption=" + getEncryption() +
                ", active=" + isActive() +
                ", uuid=" + getUniqueId() +
                '}';
    }
}
