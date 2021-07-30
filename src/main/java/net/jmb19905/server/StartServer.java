package net.jmb19905.server;


import net.jmb19905.common.util.Logger;

import java.net.BindException;

public class StartServer {

    public static Server server;

    public static void main(String[] args) throws Exception {
        try {
            server = new Server(10101);
            server.run();
        }catch (BindException e){
            Logger.log(e, "Could not bind port! Is a server already running?", Logger.Level.FATAL);
            System.exit(-1);
        }
    }

}
