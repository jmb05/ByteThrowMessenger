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
import net.jmb19905.jmbnetty.common.buffer.SimpleBuffer;
import net.jmb19905.util.crypto.Encryption;
import net.jmb19905.util.crypto.EncryptionUtility;

public class PeerMessage extends Message {

    private User receiver;

    public PeerMessage(){
        super();
    }

    public PeerMessage(User sender, User receiver, String message, long timestamp) {
        super(sender, message, timestamp);
        this.receiver = receiver;
    }

    public User getReceiver() {
        return receiver;
    }

    @Override
    public String deconstruct() {
        return "peer|" + getSender() + "|" + receiver.deconstruct() + "|" + getMessage() + "|" + timestamp;
    }

    public static PeerMessage construct(String s) {
        String[] data = s.split("\\|");
        if(!data[0].equals("peer")){
            throw new IllegalArgumentException("Tried to construct a GroupMessage to a PeerMessage");
        }
        PeerMessage peerMessage = new PeerMessage();
        peerMessage.sender = User.constructUser(data[1]);
        peerMessage.receiver = User.constructUser(data[2]);
        peerMessage.message = data[3];
        peerMessage.timestamp = Long.parseLong(data[4]);
        return peerMessage;
    }

    public static PeerMessage encrypt(PeerMessage message, Encryption encryption){
        message.setMessage(EncryptionUtility.encryptString(encryption, message.getMessage()));
        return message;
    }

    @Override
    public String getMessageDisplay() {
        return " \\b" + getSender() + "\\b \n " + message;
    }

    @Override
    public String toString() {
        return deconstruct();
    }

    @Override
    public void construct(SimpleBuffer buffer) {
        sender = buffer.get(User.class);
        receiver = buffer.get(User.class);
        message = buffer.getString();
        timestamp = buffer.getLong();
    }

    @Override
    public void deconstruct(SimpleBuffer buffer) {
        buffer.put(sender);
        buffer.put(receiver);
        buffer.putString(message);
        buffer.putLong(timestamp);
    }
}