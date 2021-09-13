package net.jmb19905.common.packets.handlers.client;

import io.netty.channel.Channel;
import net.jmb19905.client.StartClient;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.MessagePacket;
import net.jmb19905.common.util.EncryptionUtility;
import net.jmb19905.common.util.Logger;

public class ClientMessagePacketHandler extends ClientPacketHandler<MessagePacket>{

    public ClientMessagePacketHandler(MessagePacket packet) {
        super(packet);
    }

    @Override
    public void handle(EncryptedConnection encryption, Channel channel) throws IllegalSideException {
        String sender = packet.message.sender();
        String receiver = packet.message.receiver();
        String encryptedMessage = packet.message.message();
        if(receiver.equals(StartClient.client.name)) {
            Chat chat = StartClient.client.getChat(sender);
            if (chat != null) {
                String decryptedMessage = EncryptionUtility.decryptString(chat.encryption, encryptedMessage);
                chat.addMessage(new Chat.Message(sender, receiver, decryptedMessage));
                StartClient.window.appendMessage(sender, decryptedMessage);
                //Notify.create().title("ByteThrow Messenger").text("[" + sender + "] " + decryptedMessage).darkStyle().show();
            }else {
                Logger.log("Received Packet from unknown user", Logger.Level.WARN);
            }
        }else {
            Logger.log("Received Packet destined for someone else", Logger.Level.WARN);
        }
    }
}
