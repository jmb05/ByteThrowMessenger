package net.jmb19905.server.networking;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jmb19905.common.Chat;
import net.jmb19905.common.crypto.EncryptedConnection;
import net.jmb19905.common.packets.DisconnectPacket;
import net.jmb19905.common.packets.Packet;
import net.jmb19905.common.util.Logger;

import java.util.List;

/**
 * The server-side Handler for the server-client connection
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * The connection details of the client
     */
    private ClientConnection connection;

    private ServerPacketsHandler packetHandler;

    /**
     * Executes when the Connection to the Server starts
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now connected", Logger.Level.INFO);
        this.connection = new ClientConnection(new EncryptedConnection());
        this.packetHandler = new ServerPacketsHandler(this);
    }

    /**
     * Executes when the connection to the server drops
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.log("Client: \"" + ctx.channel().remoteAddress() + "\" is now disconnected", Logger.Level.INFO);
        Server.connections.remove(this);
        notifyPeersOfDisconnect();
    }

    /**
     * Executes when a packet from the client is received
     * @param msg the packet as Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        packetHandler.handlePacket(ctx, (Packet) msg);
    }

    /**
     * Tell all online peers that the client has now disconnected
     */
    private void notifyPeersOfDisconnect() {
        String disconnectedClientName = connection.getName();
        for(Chat chat : Server.getChats(disconnectedClientName)){
            List<String> clients = chat.getClients();
            for(String clientName : clients){
                if(!clientName.equals(disconnectedClientName)){
                    DisconnectPacket disconnectPacket = new DisconnectPacket();
                    disconnectPacket.name = disconnectedClientName;
                    ServerPacketsHandler.sendPacketToPeer(clientName, disconnectPacket, this);
                }
            }
        }
    }

    public ClientConnection getConnection() {
        return connection;
    }

    /**
     * Executed if an exception is caught
     * @param cause the Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.log(cause, Logger.Level.ERROR);
    }

    public static class ClientConnection {

        private String name;
        public final EncryptedConnection encryption;

        public ClientConnection(EncryptedConnection encryption){
            this.encryption = encryption;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

}