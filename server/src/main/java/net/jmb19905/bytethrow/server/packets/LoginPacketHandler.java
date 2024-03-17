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

package net.jmb19905.bytethrow.server.packets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.packets.LoginPacket;
import net.jmb19905.bytethrow.common.packets.SuccessPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.ServerManager;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class LoginPacketHandler extends PacketHandler<LoginPacket> {

    @Override
    public void handle(ChannelHandlerContext ctx, LoginPacket packet) {
        User user = packet.user;
        DatabaseManager.UserData userData = DatabaseManager.getUserDataByName(user.getUsername());
        if (userData != null) {
            if (BCrypt.checkpw(user.getPassword(), userData.password())) {
                user.removePassword();
                if (!packet.confirmIdentity) {
                    handleSuccessfulLogin(ctx, packet);
                } else {
                    sendLoginSuccess(ctx, packet);
                }
            } else {
                NetworkingUtility.sendFail(ctx, "login", "wrong_pw", "");
            }
        } else {
            NetworkingUtility.sendFail(ctx, "login", "username_not_found", user.getUsername());
        }
    }

    /**
     * Things to do when a client logs in: -> set the client name -> create client file if it doesn't exist yet ->
     * tell the Client that the login succeeded -> tell the client which conversations he has started
     *
     * @param packet the login packet containing the login packet of the client
     */
    private void handleSuccessfulLogin(ChannelHandlerContext ctx, LoginPacket packet) {
        ServerManager manager = StartServer.manager;
        if (manager.isClientOnline(packet.user)) {
            for (TcpServerHandler otherHandler : manager.getConnection().getClientConnections().keySet()) {
                User otherUser = manager.getClient(otherHandler);
                if (otherUser != null && otherUser.equals(packet.user)) {
                    SocketChannel otherSocketChannel = manager.getConnection().getClientConnections().get(otherHandler);
                    NetworkingUtility.sendFail(otherSocketChannel, "external_disconnect", "external_disconnect", "", otherHandler.getEncryption());
                    //ChannelFutureListener listener = future1 -> otherHandler.getConnection().markClosed();
                    //future.addListener(listener);
                }
            }
        }

        manager.addOnlineClient(packet.user, (TcpServerHandler) ctx.handler());
        Logger.info("Client: " + ctx.channel().remoteAddress() + " now uses name: " + manager.getClient((TcpServerHandler) ctx.handler()));

        sendLoginSuccess(ctx, packet); // confirms the login to the current client
    }

    /**
     * Sends LoginPacket to client to confirm login
     *
     * @param loginPacket the LoginPacket
     */
    private void sendLoginSuccess(ChannelHandlerContext ctx, LoginPacket loginPacket) {
        SuccessPacket loginSuccessPacket = new SuccessPacket();
        loginSuccessPacket.type = SuccessPacket.SuccessType.LOGIN;
        loginSuccessPacket.confirmIdentity = loginPacket.confirmIdentity;

        Logger.trace("Sending packet " + loginSuccessPacket + " to " + ctx.channel().remoteAddress());
        NetworkingUtility.sendPacket(loginSuccessPacket, ctx.channel(), ((TcpServerHandler) ctx.handler()).getEncryption());
    }
}
