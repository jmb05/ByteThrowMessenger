package net.jmb19905.server;


import net.jmb19905.common.util.Logger;

import java.io.File;
import java.net.BindException;

public class StartServer {

    public static Server server;

    /**
     * Starts the server
     * @param args program arguments
     */
    public static void main(String[] args) {
        File file = new File("clientData/");
        if(!file.exists() || !file.isDirectory()){
            file.mkdir();
        }
        try {
            server = new Server(10101);
            server.run();
        }catch (BindException e){
            Logger.log(e, "Could not bind port! Is a server already running?", Logger.Level.FATAL);
            System.exit(-1);
        } catch (Exception e) {
            Logger.log(e, Logger.Level.ERROR);
        }
    }

}
