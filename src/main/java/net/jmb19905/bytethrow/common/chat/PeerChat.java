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

import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.jmbnetty.common.crypto.Encryption;

import java.security.PublicKey;

public class PeerChat extends Chat{

    private Encryption encryption;
    private boolean active = false;

    /**
     * Initializes the EncryptedConnection for the client
     */
    public PeerChat(String peer){
        addClient(StartClient.manager.name);
        addClient(peer);
    }

    public PeerChat(String peer1, String peer2){
        addClient(peer1);
        addClient(peer2);
    }

    public String getOther(String peer){
        return members.stream().filter(s -> !s.equals(peer)).findFirst().orElse(null);
    }

    public void initClient(){
        encryption = new Encryption();
    }

    public void setReceiverPublicKey(PublicKey key){
        encryption.setReceiverPublicKey(key);
    }

    public Encryption getEncryption() {
        return encryption;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
