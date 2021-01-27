package net.jmb19905.messenger.packets;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.EncryptionUtility;

import java.util.ArrayList;
import java.util.List;

public class ToClientDataPacket<T extends Message> extends BTMPacket{

    public String otherUser;
    public String type;
    public List<T> data = new ArrayList<>();

    @Override
    public void handleOnClient(Connection connection) {
        if(type.equals("chatHistory")){
            String otherUser = EncryptionUtility.decryptString(MessagingClient.serverConnection, this.otherUser);
            MessagingClient.otherUsers.get(otherUser).clear();
            MessagingClient.otherUsers.get(otherUser).addMessages(data);
        }
    }

    @Override
    public void handleOnServer(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("ToClientDataPacket received on server");
    }
}
