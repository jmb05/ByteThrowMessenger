package net.jmb19905.messenger.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import net.jmb19905.messenger.packets.BTMPacket;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.logging.BTMLogger;
import net.jmb19905.messenger.util.Util;

import java.io.IOException;
import java.net.BindException;
import java.util.HashMap;

public class MessagingServer extends Listener {

    private final int port;
    private final Server server;

    public static final HashMap<Connection, ClientConnection>  clientConnectionKeys = new HashMap<>();
    public static final HashMap<String, HashMap<BTMPacket, Object[]>> messagesQueue = new HashMap<>();
    public static final HashMap<String, E2EConnection> e2eConnectedClients = new HashMap<>();

    public MessagingServer() {
        BTMLogger.trace("MessagingServer", "Initializing Server");
        this.port = ByteThrowServer.config.port;
        server = new Server();

        Util.registerPackages(server.getKryo());
        BTMLogger.trace("MessagingServer", "Registered Packages");

        server.addListener(this);
        BTMLogger.trace("MessagingServer", "Added Listener");
        BTMLogger.info("MessagingServer", "Initialized Server");
    }

    /**
     * Starts the Server
     */
    public void start() {
        BTMLogger.trace("MessagingServer", "Starting Server");
        new Thread(server).start();
        try {
            server.bind(port, port + 1);
        } catch (BindException e) {
            BTMLogger.error("MessagingServer", "Error Binding Server to port: " + port + " and/or " + (port + 1) + "! There is probably a server already running!", e);
            System.exit(-1);
        } catch (IOException e) {
            BTMLogger.error("MessagingServer", "Error binding server", e);
        }
        BTMLogger.info("MessagingServer", "Started Server");
    }

    /**
     * What to do when a Client connects to the Server
     */
    @Override
    public void connected(Connection connection) {
        BTMLogger.info("MessagingServer", "Connection established with: " + connection.getRemoteAddressTCP());
    }

    /**
     * What to do when a Client disconnects from the Server
     */
    @Override
    public void disconnected(Connection connection) {
        BTMLogger.info("MessagingServer", "Lost Connection to a Client");
        connection.close();
        clientConnectionKeys.remove(connection);
    }

    /**
     * What to do when a Package from a Client is received
     */
    @Override
    public void received(Connection connection, Object packet) {
        if (packet instanceof BTMPacket) {
            try {
                ((BTMPacket) packet).handleOnServer(connection);
            } catch (UnsupportedSideException e) {
                BTMLogger.warn("MessagingServer", "Package received on wrong side", e);
            }
        }
    }
}