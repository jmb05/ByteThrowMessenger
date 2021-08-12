package net.jmb19905.common.packets.handlers.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.common.exception.IllegalSideException;
import net.jmb19905.common.packets.LoginPacket;
import net.jmb19905.common.packets.SuccessPacket;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.NetworkingUtility;
import net.jmb19905.server.database.UserDatabaseManager;
import net.jmb19905.server.networking.Server;
import net.jmb19905.server.networking.ServerHandler;
import net.jmb19905.server.util.ClientFileManager;
import org.mindrot.jbcrypt.BCrypt;

public class LoginPacketHandler extends ServerPacketHandler<LoginPacket> {

    public LoginPacketHandler(LoginPacket packet) {
        super(packet);
    }

    @Override
    public void handle(ServerHandler handler, ServerHandler.ClientConnection connection, Channel channel) throws IllegalSideException {
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
        UserDatabaseManager.UserData userData = UserDatabaseManager.getUserDataByName(username);
        if(userData != null){
            if(BCrypt.checkpw(password, userData.password())){
                if(!packet.confirmIdentity) {
                    handleSuccessfulLogin(channel, packet, connection);
                }else {
                    sendLoginSuccess(channel, packet, connection);
                }
            }else {
                sendFail(channel, "login", "wrong_pw", "", connection);
            }
        }else {
            sendFail(channel, "login", "username_not_found", username, connection);
        }
    }

    /**
     * Checks if a register is valid
     * @param packet the LoginPacket
     */
    private void handleRegister(Channel channel, LoginPacket packet, ServerHandler.ClientConnection connection) {
        Logger.log("Client is trying to registering", Logger.Level.TRACE);
        if (UserDatabaseManager.createUser(packet.name, packet.password)) {
            handleSuccessfulLogin(channel, packet, connection);
        } else {
            sendFail(channel, "register", "register_fail", "", connection);
        }
    }

    /**
     * Things to do when a client logs in: -> set the client name -> create client file if it doesn't exist yet ->
     * tell the Client that the login succeeded -> tell the client which conversations he has started
     * @param packet the login packet containing the login packet of the client
     */
    private void handleSuccessfulLogin(Channel channel, LoginPacket packet, ServerHandler.ClientConnection connection) {
        if(Server.isClientOnline(packet.name)) {
            for(ServerHandler handler : Server.connections.keySet()){
                if(handler.getConnection().getName().equals(packet.name)){
                    SocketChannel otherSocketChannel = Server.connections.get(handler);
                    ChannelFuture future = sendFail(otherSocketChannel, "external_disconnect", "external_disconnect", "", handler.getConnection());
                    ChannelFutureListener listener = future1 -> handler.markClosed();
                    future.addListener(listener);
                }
            }
        }
        connection.setName(packet.name);
        Logger.log("Client: " + channel.remoteAddress() + " now uses name: " + connection.getName(), Logger.Level.INFO);

        sendLoginSuccess(channel, packet, connection); // confirms the login to the current client

        ClientFileManager.createClientFile(connection.getName());
    }

    /**
     * Sends LoginPacket to client to confirm login
     * @param loginPacket the LoginPacket
     */
    private void sendLoginSuccess(Channel channel, LoginPacket loginPacket, ServerHandler.ClientConnection connection) {
        SuccessPacket loginSuccessPacket = new SuccessPacket();
        loginSuccessPacket.type = loginPacket.getId();
        loginSuccessPacket.confirmIdentity = loginPacket.confirmIdentity;

        Logger.log("Sending packet " + loginSuccessPacket + " to " + channel.remoteAddress() , Logger.Level.TRACE);
        NetworkingUtility.sendPacket(loginSuccessPacket, channel, connection.encryption);
    }
}
