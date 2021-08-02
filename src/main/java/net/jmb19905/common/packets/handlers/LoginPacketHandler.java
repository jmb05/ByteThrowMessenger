package net.jmb19905.common.packets.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.ChatsPacket;
import net.jmb19905.common.packets.LoginPacket;
import net.jmb19905.common.packets.SuccessPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.ClientFileManager;
import net.jmb19905.server.Server;
import net.jmb19905.server.ServerHandler;
import net.jmb19905.server.database.SQLiteManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class LoginPacketHandler extends PacketHandler<LoginPacket>{

    @Override
    public void handleOnServer(LoginPacket packet, ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) {
        if(packet.getId().equals("register")){
            handleRegister(channel, packet, connection);
        }else if(packet.getId().equals("login")){
            handleLogin(channel, packet, connection);
        }
    }

    /**
     * Checks if a login is valid
     * @param packet the LoginPacket
     */
    private void handleLogin(Channel channel, LoginPacket packet, ServerHandler.ClientConnection connection) {
        String username = packet.name;
        String password = packet.password;
        if(SQLiteManager.hasUser(username)){
            SQLiteManager.UserData userData = SQLiteManager.getUserByName(username);
            if(BCrypt.checkpw(password, userData.password())){
                handleSuccessfulLogin(channel, packet, connection);
            }else {
                sendFail(channel, "login", "Failed to Login! - Wrong password", connection);
            }
        }else {
            sendFail(channel, "login", "Failed to Login! - User not found", connection);
        }
    }

    /**
     * Checks if a register is valid
     * @param packet the LoginPacket
     */
    private void handleRegister(Channel channel, LoginPacket packet, ServerHandler.ClientConnection connection) {
        Logger.log("Client is trying to registering", Logger.Level.TRACE);
        if (SQLiteManager.createUser(packet.name, packet.password)) {
            handleSuccessfulLogin(channel, packet, connection);
        } else {
            sendFail(channel, "register", "Failed to Register!", connection);
        }
    }

    /**
     * Things to do when a client logs in: -> set the client name -> create client file if it doesn't exist yet ->
     * tell the Client that the login succeeded -> tell the client which conversations he has started
     * @param packet the login packet containing the login packet of the client
     */
    private void handleSuccessfulLogin(Channel channel, LoginPacket packet, ServerHandler.ClientConnection connection) {
        connection.setName(packet.name);
        Logger.log("Client: " + channel.remoteAddress() + " now uses name: " + connection.getName(), Logger.Level.INFO);

        sendLoginSuccess(channel, packet, connection); // confirms the login to the current client

        ClientFileManager.createClientFile(connection.getName());

        sendChatsPacket(channel, connection);
    }

    /**
     * Sends LoginPacket to client to confirm login
     * @param loginPacket the LoginPacket
     */
    private void sendLoginSuccess(Channel channel, LoginPacket loginPacket, ServerHandler.ClientConnection connection) {
        SuccessPacket loginSuccessPacket = new SuccessPacket();
        loginSuccessPacket.type = loginPacket.getId();

        Logger.log("Sending packet LoginSuccess to " + channel.remoteAddress() , Logger.Level.TRACE);
        NetworkingUtility.sendPacket(loginSuccessPacket, channel, connection.encryption);
    }

    private void sendChatsPacket(Channel channel, ServerHandler.ClientConnection connection){
        String clientName = connection.getName();
        ChatsPacket packet = new ChatsPacket();
        packet.names = getPeerNames(clientName);

        Logger.log("Sending packet " + packet + " to " + channel.remoteAddress(), Logger.Level.TRACE);
        NetworkingUtility.sendPacket(packet, channel, connection.encryption);
    }

    private String[] getPeerNames(String clientName) {
        List<String> names = new ArrayList<>();
        for(int i=0;i<Server.chats.size();i++){
            List<String> chatParticipants = Server.chats.get(i).getClients();
            if(chatParticipants.contains(clientName)){
                for(String otherName : chatParticipants) {
                    if(!otherName.equals(clientName)) {
                        names.add(otherName);
                    }
                }
            }
        }
        return names.toArray(new String[0]);
    }

    @Override
    public void handleOnClient(LoginPacket packet, EncryptedConnection encryption, Channel channel) {

    }
}
