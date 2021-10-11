/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.bytethrow.common.packets.RegisterPacket;
import net.jmb19905.bytethrow.common.packets.SuccessPacket;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.bytethrow.server.StartServer;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.bytethrow.server.networking.ServerManager;
import net.jmb19905.jmbnetty.client.tcp.TcpClientHandler;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.util.Logger;

public class RegisterPacketHandler extends PacketHandler {
    @Override
    public void handleOnServer(ChannelHandlerContext ctx, Packet packet, TcpServerHandler tcpServerHandler) {
        RegisterPacket registerPacket = (RegisterPacket) packet;
        Logger.trace("Client is trying to registering");
        if (DatabaseManager.createUser(registerPacket.name, registerPacket.password)) {
            handleSuccessfulRegister(ctx.channel(), registerPacket, tcpServerHandler);
        } else {
            NetworkingUtility.sendFail(ctx.channel(), "register", "register_fail", "", tcpServerHandler);
        }
    }

    /**
     * Things to do when a client logs in: -> set the client name -> create client file if it doesn't exist yet ->
     * tell the Client that the login succeeded -> tell the client which conversations he has started
     * @param packet the login packet containing the login packet of the client
     */
    private void handleSuccessfulRegister(Channel channel, RegisterPacket packet, TcpServerHandler handler) {
        ServerManager manager = StartServer.manager;
        if(manager.isClientOnline(packet.name)) {
            for(TcpServerHandler otherHandler : ((TcpServerConnection) handler.getConnection()).getClientConnections().keySet()){
                if(manager.getClientName(otherHandler).equals(packet.name)){
                    SocketChannel otherSocketChannel = ((TcpServerConnection) handler.getConnection()).getClientConnections().get(otherHandler);
                    ChannelFuture future = NetworkingUtility.sendFail(otherSocketChannel, "external_disconnect", "external_disconnect", "", otherHandler);
                    ChannelFutureListener listener = future1 -> otherHandler.getConnection().markClosed();
                    future.addListener(listener);
                }
            }
        }
        manager.addOnlineClient(packet.name, handler);
        Logger.info("Client: " + channel.remoteAddress() + " now uses name: " + manager.getClientName(handler));

        sendRegisterSuccess(channel, packet, handler); // confirms the register to the current client
    }

    /**
     * Sends LoginPacket to client to confirm login
     * @param loginPacket the LoginPacket
     */
    private void sendRegisterSuccess(Channel channel, RegisterPacket loginPacket, TcpServerHandler handler) {
        SuccessPacket loginSuccessPacket = new SuccessPacket();
        loginSuccessPacket.type = "register";
        loginSuccessPacket.confirmIdentity = loginPacket.confirmIdentity;

        Logger.trace("Sending packet " + loginSuccessPacket + " to " + channel.remoteAddress());
        NetworkingUtility.sendPacket(loginSuccessPacket, channel, handler.getEncryption());
    }

    @Override
    public void handleOnClient(ChannelHandlerContext channelHandlerContext, Packet packet, TcpClientHandler tcpClientHandler) throws IllegalSideException {
        throw new IllegalSideException("RegisterPacket received on Client");
    }
}
