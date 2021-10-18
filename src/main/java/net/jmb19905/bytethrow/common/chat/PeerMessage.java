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

import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.crypto.EncryptionUtility;

public class PeerMessage extends Message {

    private String sender;
    private String receiver;

    private PeerMessage(){
        super();
    }

    public PeerMessage(String sender, String receiver, String message, long timestamp) {
        super(message, timestamp);
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    @Override
    public String deconstruct() {
        return "peer|" + sender + "|" + receiver + "|" + getMessage() + "|" + timestamp;
    }

    public static PeerMessage construct(String s) {
        String[] data = s.split("\\|");
        if(!data[0].equals("peer")){
            throw new IllegalArgumentException("Tried to construct a GroupMessage to a PeerMessage");
        }
        PeerMessage peerMessage = new PeerMessage();
        peerMessage.sender = data[1];
        peerMessage.receiver = data[2];
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
        return " \\b<" + sender + ">\\b " + message;
    }

    @Override
    public String toString() {
        return deconstruct();
    }
}