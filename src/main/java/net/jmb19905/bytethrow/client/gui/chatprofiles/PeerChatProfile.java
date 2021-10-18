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
import net.jmb19905.bytethrow.client.chat.ClientPeerChat;
import net.jmb19905.bytethrow.common.chat.PeerMessage;

public class PeerChatProfile extends AbstractChatProfile<PeerMessage> {

    private boolean connected = false;

    public PeerChatProfile(String other, ClientPeerChat chat){
        super(other, chat.getUniqueId());
        setMessages(chat.getMessages());
    }

    public PeerChatProfile(ClientPeerChat chat) {
        super(chat.getOther(StartClient.manager.name), chat.getUniqueId());
        setMessages(chat.getMessages());
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}