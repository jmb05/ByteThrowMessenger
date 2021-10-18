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

public class GroupMessage extends Message {

    private String sender;
    private String groupName;

    private GroupMessage(){
        super();
    }

    public GroupMessage(String sender, String groupName, String message, long timestamp) {
        super(message, timestamp);
        this.sender = sender;
        this.groupName = groupName;
    }

    public String getSender() {
        return sender;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String deconstruct() {
        return "group|" + sender + "|" + groupName + "|" + getMessage() + "|" + timestamp;
    }

    public static GroupMessage construct(String s) {
        String[] data = s.split("\\|");
        if(!data[0].equals("group")){
            throw new IllegalArgumentException("Tried to construct a PeerMessage to a GroupMessage");
        }
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.sender = data[1];
        groupMessage.groupName = data[2];
        groupMessage.message = data[3];
        groupMessage.timestamp = Long.parseLong(data[4]);
        return groupMessage;
    }

    @Override
    public String getMessageDisplay() {
        return " \\b<" + sender + ">\\b " + message;
    }

    @Override
    public String toString() {
        return deconstruct();
    }
}
