package net.jmb19905.client;

import net.jmb19905.client.util.GenClientData;

public class ClientMain {

    public static void main(String[] args) {
        if(args.length == 0){
            StartClient.main(args);
        }else if(args[0].equals("genData")){
            GenClientData.main(args);
        }else {
            StartClient.main(args);
        }
    }

}
