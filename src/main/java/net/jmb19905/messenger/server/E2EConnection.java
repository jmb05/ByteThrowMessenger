package net.jmb19905.messenger.server;

import java.util.TreeMap;

/**
 * The Connection between two Client from the Servers POV
 * The server will hold the encrypted History of the Client connections so that the client can access them if on a new device of if he lost it
 */
public class E2EConnection {

    private TreeMap<String, String> history;

}
