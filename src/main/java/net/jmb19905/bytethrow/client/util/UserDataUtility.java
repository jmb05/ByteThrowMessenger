package net.jmb19905.bytethrow.client.util;

import net.jmb19905.util.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UserDataUtility {

    public static void writeUserFile(String username, String password, File outputFile){
        if(!outputFile.exists()){
            if(!outputFile.getParentFile().mkdirs()){
                Logger.error("Error creating userdata directory");
            }else {
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
        }
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(Base64.getEncoder().encode((username + "|" + password).getBytes()));
            outputStream.flush();
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }

    public static String[] readUserFile(File inputFile){
        if(!inputFile.exists()){
            return new String[0];
        }
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            byte[] bytes = inputStream.readAllBytes();
            String decoded = new String(Base64.getDecoder().decode(bytes), StandardCharsets.UTF_8);
            return decoded.split("\\|");
        } catch (IOException ex) {
            Logger.error(ex);
            return new String[0];
        }
    }

}
