package net.jmb19905.bytethrow.server.util;

import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.serial.ChatSerial;
import net.jmb19905.bytethrow.server.StartServer;

public class ClientDataFilesManager {

    public static void loadChats(){
        StartServer.manager.setChats(ChatSerial.readAllChats());
    }

    public static void writeChats(String name){
        for(Chat chat : StartServer.manager.getChats(name)){
            ChatSerial.write(chat);
        }
    }

    public static void writeChats(){
        for(Chat chat : StartServer.manager.getChats()){
            ChatSerial.write(chat);
        }
    }

}
