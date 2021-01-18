package net.jmb19905.messenger.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import net.jmb19905.messenger.packages.EMPackage;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import java.io.IOException;
import java.net.BindException;
import java.util.HashMap;

public class MessagingServer extends Listener {

    private final int port;
    private final Server server;

    public static final HashMap<Connection, ClientConnection>  clientConnectionKeys = new HashMap<>();
    public static final HashMap<String, HashMap<EMPackage, Object[]>> messagesQueue = new HashMap<>();
    public static final HashMap<String, E2EConnection> e2eConnectedClients = new HashMap<>();

    public MessagingServer() {
        EMLogger.trace("MessagingServer", "Initializing Server");
        this.port = ServerMain.config.port;
        server = new Server();

        Util.registerPackages(server.getKryo());
        EMLogger.trace("MessagingServer", "Registered Packages");

        server.addListener(this);
        EMLogger.trace("MessagingServer", "Added Listener");
        EMLogger.info("MessagingServer", "Initialized Server");
    }

    /**
     * Starts the Server
     */
    public void start() {
        EMLogger.trace("MessagingServer", "Starting Server");
        new Thread(server).start();
        try {
            server.bind(port, port + 1);
        } catch (BindException e) {
            EMLogger.error("MessagingServer", "Error Binding Server to port: " + port + " and/or " + (port + 1) + "! There is probably a server already running!", e);
            System.exit(-1);
        } catch (IOException e) {
            EMLogger.error("MessagingServer", "Error binding server", e);
        }
        EMLogger.info("MessagingServer", "Started Server");
    }

    /**
     * What to do when a Client connects to the Server
     */
    @Override
    public void connected(Connection connection) {
        EMLogger.info("MessagingServer", "Connection established with: " + connection.getRemoteAddressTCP());
    }

    /**
     * What to do when a Client disconnects from the Server
     */
    @Override
    public void disconnected(Connection connection) {
        EMLogger.info("MessagingServer", "Lost Connection to a Client");
        connection.close();
        clientConnectionKeys.remove(connection);
    }

    /**
     * What to do when a Package from a Client is received
     */
    @Override
    public void received(Connection connection, Object o) {
        if (o instanceof EMPackage) {
            try {
                ((EMPackage) o).handleOnServer(connection);
            } catch (UnsupportedSideException e) {
                EMLogger.warn("MessagingServer", "Package received on wrong side", e);
            }
        }
    }
}