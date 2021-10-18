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

import net.jmb19905.bytethrow.common.chat.Message;
import net.jmb19905.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractChatProfile<M extends Message> implements IChatProfile<M>{

    private final UUID id;
    private String displayName;
    private List<M> messages = new ArrayList<>();

    protected AbstractChatProfile(String displayName, UUID id) {
        this.displayName = displayName;
        this.id = id;
    }

    public void setDisplayName(String displayName, UUID id) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setMessages(List<M> messages) {
        this.messages = messages;
    }

    public void addMessage(M message){
        if(!this.messages.contains(message)) {
            this.messages.add(message);
            if (messages.size() > 1) {
                messages = messages.stream().sorted().collect(Collectors.toList());
            }
        }
    }

    public M getMessage(long timestamp){
        return messages.stream().filter(m -> m.getTimestamp() == timestamp).findFirst().orElse(null);
    }

    @Override
    public List<M> getMessages() {
        return messages;
    }

    @Override
    public UUID getUniqueID() {
        return id;
    }
}
