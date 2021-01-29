package net.jmb19905.messenger.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jmb19905.messenger.packets.BTMPacket;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.FileUtility;
import net.jmb19905.messenger.util.Util;
import net.jmb19905.messenger.util.logging.BTMLogger;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagingServer extends Listener {

    private final int port;
    private final Server server;

    public static final HashMap<Connection, ClientConnection>  clientConnectionKeys = new HashMap<>();
    public static final HashMap<String, HashMap<BTMPacket, Object[]>> messagesQueue = new HashMap<>();
    public static List<E2EConnection> e2eToBeConfirmed = new ArrayList<>();
    public static List<E2EConnection> e2eConnectedClients = new ArrayList<>();
    public static List<String> watchList = new ArrayList<>();

    public MessagingServer() {
        BTMLogger.trace("MessagingServer", "Initializing Server");
        this.port = ByteThrowServer.config.port;
        e2eConnectedClients = readAllE2EConnections();
        e2eToBeConfirmed = readAllUnconfirmedConnections();
        server = new Server();
        readWatchlist();

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

    public static void writeE2EConnectionsToFile(){
        File folder = new File("serverdata/connections/");
        folder.mkdirs();
        FileUtility.writeDirectory(folder, e2eConnectedClients);
    }

    public static void writeUnconfirmedConnectionsToFile(){
        File folder = new File("serverdata/unconfirmed/");
        folder.mkdirs();
        FileUtility.writeDirectory(folder, e2eToBeConfirmed);
    }

    private List<E2EConnection> readAllE2EConnections(){
        try {
            File folder = new File("serverdata/connections/");
            folder.mkdirs();
            if(folder != null && folder.exists()) {
                return FileUtility.readDirectory(folder);
            }
        } catch (IOException e) {
            BTMLogger.warn("MessagingServer", "Error loading connections");
        }
        return new ArrayList<>();
    }

    private List<E2EConnection> readAllUnconfirmedConnections(){
        try {
            File folder = new File("/serverdata/unconfirmed/");
            folder.mkdirs();
            if(folder != null && folder.exists()) {
                return FileUtility.readDirectory(folder);
            }
        } catch (IOException e) {
            BTMLogger.warn("MessagingServer", "Error loading unconfirmed connections");
        }
        return new ArrayList<>();
    }

    public static void deleteUnconfirmedConnection(E2EConnection e2EConnection){
        File folder = new File("/serverdata/unconfirmed/" + e2EConnection.getFileName() + "/");
        System.out.println(FileUtility.deleteDirectory(folder));
        System.out.println("Deleted: " + folder.getAbsolutePath());
    }

    public static void saveWatchlist(){
        File watchListFile = new File("serverdata/watchlist.json");
        FileUtility.createFile(watchListFile);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(watchListFile, watchList);
        } catch (IOException e) {
            BTMLogger.warn("MessagingServer", "Error saving watchlist");
        }
    }

    private static void readWatchlist(){
        File watchListFile = new File("serverdata/watchlist.json");
        if(!watchListFile.exists()) return;
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<String>> typeReference = new TypeReference<List<String>>() {};
        try {
            watchList = objectMapper.readValue(watchListFile, typeReference);
        } catch (IOException e) {
            BTMLogger.warn("MessagingServer", "Error reading watchlist");
        }
    }

}