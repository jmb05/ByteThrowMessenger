package net.jmb19905.common.packets.handlers.client;

import io.netty.channel.Channel;
import net.jmb19905.client.ClientMain;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.ConnectPacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;

import java.security.spec.InvalidKeySpecException;

public class ClientConnectPacketHandler extends ClientPacketHandler<ConnectPacket>{

    protected ConnectPacket packet;

    public ClientConnectPacketHandler(ConnectPacket packet) {
        super(packet);
        this.packet = packet;
    }

    @Override
    public void handle(EncryptedConnection encryption, Channel channel) {
        String peerName = packet.name;
        byte[] encodedPeerKey = packet.key;

        try {
            if(ClientMain.client.getChat(peerName) == null) {
                handleNewChatRequestClient(packet, channel, encryption, peerName, encodedPeerKey);
            }else if(ClientMain.client.getChat(peerName) != null) {
                handleConnectToExistingChatRequestClient(packet, channel, encryption, peerName, encodedPeerKey);
            }
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.ERROR);
            ClientMain.window.appendLine("Error connecting with: " + peerName);
        }
    }

    private void handleConnectToExistingChatRequestClient(ConnectPacket packet, Channel channel, EncryptedConnection encryption, String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Logger.log("Peer tried create a Chat that already exists", Logger.Level.WARN);
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_CONNECT){
            activateEncryption(peerName, encodedPeerKey);
        }else if(packet.connectType == ConnectPacket.ConnectType.FIRST_RECONNECT){
            activateEncryption(peerName, encodedPeerKey);

            EncryptedConnection chatEncryption = ClientMain.client.getChat(peerName).encryption;

            packet.name = peerName;
            packet.key = chatEncryption.getPublicKey().getEncoded();
            packet.connectType = ConnectPacket.ConnectType.REPLY_RECONNECT;

            NetworkingUtility.sendPacket(packet, channel, encryption);
        }else if(packet.connectType == ConnectPacket.ConnectType.REPLY_RECONNECT){
            activateEncryption(peerName, encodedPeerKey);
        }
    }

    private void activateEncryption(String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        Chat chat = ClientMain.client.getChat(peerName);
        chat.setActive(true);
        ClientMain.window.setPeerStatus(peerName, true);

        chat.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
        ClientMain.window.appendLine("Connection to " + peerName + " encrypted");
    }

    private void handleNewChatRequestClient(ConnectPacket packet, Channel channel, EncryptedConnection encryption, String peerName, byte[] encodedPeerKey) throws InvalidKeySpecException {
        if(packet.connectType == ConnectPacket.ConnectType.FIRST_CONNECT) {
            Chat chat = new Chat();
            chat.addClient(ClientMain.client.name);
            chat.addClient(peerName);
            chat.initClient();
            chat.setActive(true);
            ClientMain.client.chats.add(chat);

            ClientMain.window.addPeer(peerName);
            ClientMain.window.setPeerStatus(peerName, true);

            chat.encryption.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(encodedPeerKey));
            ClientMain.window.appendLine("Connection to " + peerName + " encrypted");


            Logger.log("Starting E2E Encryption to: " + peerName, Logger.Level.INFO);
            ConnectPacket replyPacket = new ConnectPacket();
            replyPacket.name = peerName;
            replyPacket.key = chat.encryption.getPublicKey().getEncoded();
            replyPacket.connectType = ConnectPacket.ConnectType.REPLY_CONNECT;
            Logger.log("Sending packet ConnectPacket to " + peerName, Logger.Level.TRACE);

            NetworkingUtility.sendPacket(replyPacket, channel, encryption);
        }else {
            Logger.log("What is this Client even doing with his life?", Logger.Level.WARN);
        }
    }

}
