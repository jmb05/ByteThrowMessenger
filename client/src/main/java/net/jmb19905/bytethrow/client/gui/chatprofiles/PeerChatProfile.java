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

package net.jmb19905.bytethrow.client.gui.chatprofiles;

import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.client.ClientPeerChat;

public class PeerChatProfile extends AbstractChatProfile {

    private final User peer;
    private boolean connected = false;

    public PeerChatProfile(User peer, ClientPeerChat chat){
        super(peer.getUsername(), chat.getUniqueId());
        this.peer = peer;
        setMessages(chat.getMessages());
    }

    public PeerChatProfile(ClientPeerChat chat) {
        super(chat.getOther(StartClient.manager.user).getUsername(), chat.getUniqueId());
        peer = chat.getOther(StartClient.manager.user);
        setMessages(chat.getMessages());
    }

    public User getPeer() {
        return peer;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}