/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.client.packets;

import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.client.ClientPeerChat;
import net.jmb19905.bytethrow.common.packets.ConnectPacket;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.PacketHandler;
import net.jmb19905.util.Logger;
import net.jmb19905.util.crypto.Encryption;
import net.jmb19905.util.crypto.EncryptionUtility;

import java.security.spec.InvalidKeySpecException;

public class ConnectPacketHandler implements PacketHandler<ConnectPacket> {

    @Override
    public void handle(HandlingContext ctx, ConnectPacket packet) {
        User peer = packet.user;
        byte[] encodedPeerKey = packet.key;

        try {
            if (StartClient.manager.getChat(peer) == null) {
                handleNewChatRequestClient(packet, peer, encodedPeerKey);
            } else if (StartClient.manager.getChat(peer) != null) {
                handleConnectToExistingChatRequestClient(packet, peer, encodedPeerKey);
            }
        } catch (InvalidKeySpecException e) {
            Logger.error(e);
        }
    }

    private void handleConnectToExistingChatRequestClient(ConnectPacket packet, User peer, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Logger.warn("Peer tried create a Chat that already exists");
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT) {
            activateEncryption(peer, encodedPeerKey);
        } else if (packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT) {
            activateEncryption(peer, encodedPeerKey);

            Encryption chatEncryption = StartClient.manager.getChat(peer).getEncryption();

            packet.user = peer;
            packet.key = chatEncryption.getPublicKey().getEncoded();
            packet.connectType = ConnectPacket.ConnectType.REPLY_RECONNECT;

            StartClient.manager.send(packet);
        } else if (packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT) {
            activateEncryption(peer, encodedPeerKey);
        }
    }

    private void activateEncryption(User peer, byte[] encodedPeerKey) {
        ClientPeerChat chat = StartClient.manager.getChat(peer);
        chat.setActive(true);
        StartClient.guiManager.setPeerStatus(chat, true);

        chat.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
    }

    private void handleNewChatRequestClient(ConnectPacket packet, User peer, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if (packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            ClientPeerChat chat = new ClientPeerChat(StartClient.manager.user, peer);
            chat.initClient();
            chat.setActive(true);
            StartClient.manager.addChat(chat);

            StartClient.guiManager.setPeerStatus(chat, true);

            chat.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));


            Logger.info("Starting E2E Encryption to: " + peer.getUsername());
            ConnectPacket replyPacket = new ConnectPacket();
            replyPacket.user = peer;
            replyPacket.key = chat.getEncryption().getPublicKey().getEncoded();
            replyPacket.connectType = ConnectPacket.ConnectType.REPLY_CONNECT;
            Logger.trace("Sending packet ConnectPacket to " + peer);

            StartClient.manager.send(replyPacket);
        } else {
            Logger.warn("What is this Client even doing with his life?");
        }
    }
}
