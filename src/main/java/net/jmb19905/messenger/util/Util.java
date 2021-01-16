package net.jmb19905.messenger.util;

import com.esotericsoftware.kryo.Kryo;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.crypto.exception.InvalidNodeException;
import net.jmb19905.messenger.messages.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Util {

    public static PublicKey createPublicKeyFromData(byte[] encodedKey) throws InvalidKeySpecException {
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException e) {
            EMLogger.error("Util", "Error retrieving PublicKey", e);
            return null;
        }
    }

    public static PrivateKey createPrivateKeyFromData(byte[] encodedKey){
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            EMLogger.error("Util", "Error retrieving PrivateKey", e);
            return null;
        }
    }

    public static HashMap<String, Node> loadNodes(String filePath){
        HashMap<String, Node> output = new HashMap<>();
        File file = new File(filePath);
        try {
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<String> lines = new ArrayList<>();
            String line = reader.readLine();
            while(line != null){
                System.out.println("Line: {" + line + "}");
                if(!line.equals("") && line != null) {
                    lines.add(line);
                }
                line = reader.readLine();
            }
            for(int i=0;i<lines.size();i++){
                String[] parts = lines.get(i).split("] \\[");
                try {
                    String username = parts[0];
                    byte[] publicKey = readByteArray(parts[1]);
                    byte[] privateKey = readByteArray(parts[2]);
                    byte[] sharedSecret = readByteArray(parts[3]);
                    output.put(username, new Node(publicKey, privateKey, sharedSecret));
                }catch (ArrayIndexOutOfBoundsException e){
                    EMLogger.warn("Util", "Error loading Node in line " + i);
                }catch (InvalidNodeException e){
                    lines.get(i);
                    EMLogger.warn("Util", "Skipped loading invalid Node");
                }
            }
            reader.close();
        } catch (IOException e) {
            EMLogger.error("MessagingClient", "Error loading Nodes from File", e);
        }
        return output;
    }

    public static byte[] readByteArray(String arrayAsString){
        if(!arrayAsString.startsWith("[") || !arrayAsString.endsWith("]")){
            return new byte[0];
        }
        String[] parts = arrayAsString.replaceAll("\\[", "").replaceAll("]", "").split(", ");
        byte[] output = new byte[parts.length];
        for(int i=0;i<parts.length;i++){
            try {
                output[i] = (byte) Integer.parseInt(parts[i]);
            }catch (NumberFormatException e){
                EMLogger.warn("Util", "String array does not represent a byte array", e);
                return new byte[0];
            }
        }
        return output;
    }

    public static void saveNodes(HashMap<String, Node> nodes, String filePath){
        File file = new File(filePath);
        try {
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for(String key : nodes.keySet()){
                byte[] sharedSecret = new byte[0];
                if(nodes.get(key).getSharedSecret() != null){
                    sharedSecret = nodes.get(key).getSharedSecret();
                }
                String line = "[" + key + "] " + Arrays.toString(nodes.get(key).getPublicKey().getEncoded()) + " " + Arrays.toString(nodes.get(key).getPrivateKey().getEncoded()) + " " + Arrays.toString(sharedSecret) + "";
                writer.write(line);
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            EMLogger.error("Util", "Error writing Nodes to File", e);
        }catch (NullPointerException e){
            EMLogger.info("Util", "No other User Connections");
        }
    }

    public static void registerMessages(Kryo kryo){
        kryo.register(LoginPublicKeyMessage.class);
        kryo.register(byte[].class);
        kryo.register(LoginMessage.class);
        kryo.register(RegisterMessage.class);
        kryo.register(UsernameAlreadyExistMessage.class);
        kryo.register(RegisterSuccessfulMessage.class);
        kryo.register(NotRegisteredMessage.class);
        kryo.register(LoginSuccessMessage.class);
        kryo.register(ConnectWithOtherUserMessage.class);
        kryo.register(DataMessage.class);
        kryo.register(LoginFailedMessage.class);
    }

    public static String encryptString(Node node, String value){
        return new String(node.encrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decryptString(Node node, String value){
        return new String(node.decrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

}
