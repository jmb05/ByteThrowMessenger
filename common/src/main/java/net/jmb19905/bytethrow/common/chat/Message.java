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

package net.jmb19905.bytethrow.common.chat;

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.jmbnetty.common.buffer.BufferSerializable;

public abstract class Message implements Comparable<Message>, BufferSerializable {

    protected User sender;
    protected String message;
    protected long timestamp;

    protected Message(){}

    public Message(User sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public User getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public abstract String deconstruct();

    public abstract String getMessageDisplay();

    @Override
    public int compareTo(Message o) {
        return Long.compare(timestamp, o.timestamp);
    }
}
