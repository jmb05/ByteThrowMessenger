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
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.jmbnetty.common.crypto.Encryption;

import java.security.PublicKey;
import java.util.UUID;

public class PeerChat extends AbstractChat {

    private Encryption encryption;
    private boolean active = false;

    /**
     * Initializes the EncryptedConnection for the client
     */
    public PeerChat(User peer) {
        addClient(StartClient.manager.user);
        addClient(peer);
    }

    public PeerChat(User peer1, User peer2) {
        addClient(peer1);
        addClient(peer2);
    }

    public PeerChat(User peer, UUID uuid){
        super(uuid);
        addClient(StartClient.manager.user);
        addClient(peer);
    }

    public PeerChat(User peer1, User peer2, UUID uuid){
        super(uuid);
        addClient(peer1);
        addClient(peer2);
    }

    public User getOther(User peer) {
        return members.stream().filter(u -> !u.equals(peer)).findFirst().orElse(null);
    }

    public void initClient() {
        encryption = new Encryption();
    }

    public void setReceiverPublicKey(PublicKey key) {
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

    @Override
    public String toString() {
        return "PeerChat{" +
                "members=" + members +
                ", encryption=" + encryption +
                ", active=" + active +
                ", uuid=" + getUniqueId() +
                '}';
    }
}
