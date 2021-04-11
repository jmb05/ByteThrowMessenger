package net.jmb19905.messenger.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.util.FileUtility;
import net.jmb19905.messenger.util.Variables;
import net.jmb19905.messenger.util.logging.BTMLogger;

import java.io.*;
import java.util.List;

public class ClientSerializationUtils {

    /**
     * Reads the user data from "/userdata/user.dat"
     */
    public static UserSession readUserData() {
        UserSession userSession = new UserSession();
        try {
            File file = new File(Variables.dataDirectory + "userdata/user.dat");
            if (FileUtility.createFile(file)) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                try {
                    if (!reader.readLine().equals("UserSession:")) {
                        return userSession;
                    }
                    userSession.username = reader.readLine();
                    userSession.password = reader.readLine();
                    reader.close();
                } catch (NullPointerException e) {
                    BTMLogger.warn("MessagingClient", "No UserSession found in file user.dat - login required");
                }
            }else {
                BTMLogger.warn("MessagingClient", "Error creating userdata file");
            }
        } catch (IOException e) {
            BTMLogger.warn("MessagingClient", "Error reading userdata", e);
        }
        return userSession;
    }

    /**
     * Writes the userdata to "/userdata/user.dat"
     */
    public static void writeUserData(UserSession userdata) {
        if (!userdata.username.equals("") && !userdata.password.equals("")) {
            try {
                File file = new File("userdata/user.dat");
                if(FileUtility.createFile(file)) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write("UserSession:\n");
                    writer.write(userdata.username + "\n");
                    writer.write(userdata.password + "\n");
                    writer.close();
                }else{
                    BTMLogger.warn("MessagingClient", "Error creating userdata file");
                }
            } catch (IOException e) {
                BTMLogger.info("MessagingClient", "Error writing userdata", e);
            }
        } else {
            BTMLogger.warn("MessagingClient", "Can't write UserSession to file 'user.dat'! Incomplete data");
        }
    }

    /**
     * Deletes the userdata
     */
    public static void wipeUserData(UserSession userdata) {
        userdata.username = "";
        userdata.password = "";
        try {
            File userDat = new File("userdata/user.dat");
            if (userDat.exists()) {
                userDat.delete();
                userDat.createNewFile();
            }
        } catch (IOException e) {
            BTMLogger.warn("MessagingClient", "Cannot wipe userdata (user.dat)");
        }
    }

    public static void serializeUserConnection(String name, File directory, UserConnection connection){
        try {
            serializeEncryptedConnection(name, directory, connection.getEncryptedConnection());
            serializeChatHistory(name, directory, connection.getMessages());
        }catch (IOException e){
            BTMLogger.warn("ClientSerializingUtils", "Error serializing data... Next login might require fetch from server", e);
        }
    }

    public static UserConnection deserializeUserConnection(String name, File directory) throws IOException {
        UserConnection connection = new UserConnection(name, deserializeEncryptedConnection(name, directory));
        connection.addMessages(deserializeChatHistory(name, directory));
        return connection;
    }

    public static void serializeEncryptedConnection(String name, File directory, EncryptedConnection connection) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(directory.getAbsolutePath() + "/" + name + "/conn.json");
        FileUtility.createFile(file);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, connection);
    }

    public static EncryptedConnection deserializeEncryptedConnection(String name, File directory) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(directory.getAbsolutePath() + "/" + name + "/conn.json");
        FileUtility.createFile(file);
        return mapper.readValue(file, EncryptedConnection.class);
    }

    public static void serializeChatHistory(String name, File directory, List<Message> messagesHistory) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(directory.getAbsolutePath() + "/" + name + "/hist.json");
        FileUtility.createFile(file);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, messagesHistory);
    }

    public static List<Message> deserializeChatHistory(String name, File directory) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(directory.getAbsolutePath() + "/" + name + "/hist.json");
        FileUtility.createFile(file);
        TypeReference<List<Message>> listTypeReference = new TypeReference<List<Message>>() {};
        return mapper.readValue(new File(directory.getAbsolutePath() + "/" + name + "/hist.json"), listTypeReference);
    }

}
