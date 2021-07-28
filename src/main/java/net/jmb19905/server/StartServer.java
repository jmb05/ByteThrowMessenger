package net.jmb19905.server;


public class StartServer {

    public static Server server;

    public static void main(String[] args) throws Exception {
        server = new Server(10101);
        server.run();
    }

}
