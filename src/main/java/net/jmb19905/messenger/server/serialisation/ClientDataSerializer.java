package net.jmb19905.messenger.server.serialisation;

import java.io.File;

public class ClientDataSerializer {

    private String user1Name;
    private String user2Name;

    private File directory;

    public ClientDataSerializer(String user1Name, String user2Name){
        this.user1Name = user1Name;
        this.user2Name = user2Name;
        this.directory = new File("clientData/" + user1Name + "-" + user2Name + "/");
        if(!directory.exists() || !directory.isDirectory()){
            directory.mkdirs();
        }
    }

    public void serializeClientData(){

    }

    public String getUser1Name() {
        return user1Name;
    }

    public String getUser2Name() {
        return user2Name;
    }
}
