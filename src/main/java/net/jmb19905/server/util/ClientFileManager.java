package net.jmb19905.server.util;

import net.jmb19905.common.Chat;
import net.jmb19905.common.util.Logger;
import net.jmb19905.server.database.UserDatabaseManager;
import net.jmb19905.server.networking.Server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClientFileManager {

    /**
     * creates the client file if it doesn't exist yet
     */
    public static void createClientFile(String name) {
        try {
            File clientFile = new File("clientData/" + name + ".dat");
            if(!clientFile.exists()){
                clientFile.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAllClientFiles(){
        File clientFolder = new File("clientData/");
        for(File clientFile : clientFolder.listFiles()){
            if(!clientFile.isDirectory()){
                String clientName = clientFile.getName().replaceAll("\\.dat", "");
                if(UserDatabaseManager.hasUser(clientName)) {
                    loadClientFile(clientFile);
                }else {
                    clientFile.delete();
                }
            }
        }
    }

    public static void loadClientFile(File file){
        String name = file.getName().replace(".dat", "");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            List<String> lines = new ArrayList<>();
            String line;
            do {
                line = reader.readLine();
                lines.add(line);
            }while ((line != null));

            for(String otherName : lines) {
                if(otherName != null) {
                    if(UserDatabaseManager.hasUser(otherName)) {
                        Chat chat = new Chat();
                        chat.addClient(name);
                        chat.addClient(otherName);

                        for (Chat otherChat : Server.chats) {
                            if (otherChat.clientsEquals(chat)) {
                                return;
                            }
                        }

                        Server.chats.add(chat);

                        Logger.log("Read Chat:" + chat, Logger.Level.DEBUG);
                    }else {
                        Logger.log("Read User from Data File that does not exist!", Logger.Level.WARN);
                    }
                }
            }
        } catch (IOException e) {
            Logger.log(e, "Error reading client File for: " + name, Logger.Level.WARN);
        }
        writeChatsToFile(name);
    }

    public static void writeChatsToFile(String name){
        File clientFile = new File("clientData/" + name + ".dat");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clientFile))){
            for(Chat chat : Server.getChats(name)){
                writeChatToFile(writer, name, chat);
            }
        }catch (IOException e){
            Logger.log(e, "Error reading client File for: " + name, Logger.Level.WARN);
        }
    }

    private static void writeChatToFile(BufferedWriter writer, String name, Chat chat) throws IOException {
        for(String otherName : chat.getClients()){
            if(!name.equals(otherName)){
                writer.write(otherName + "\n");
                writer.flush();
                Logger.log("Wrote: " + otherName + " to " + name, Logger.Level.DEBUG);
            }
        }
    }

}
