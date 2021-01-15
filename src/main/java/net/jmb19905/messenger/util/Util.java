package net.jmb19905.messenger.util;

import com.esotericsoftware.kryo.Kryo;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class Util {

    public static PublicKey createPublicKeyFromData(byte[] encodedKey){
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            EMLogger.error("Util", "Error retrieving PublicKey", e);
            return null;
        }
    }

    public static PrivateKey createPrivateKeyFromData(byte[] encodedKey){
        try {
            KeyFactory factory = KeyFactory.getInstance("EC");
            return factory.generatePrivate(new X509EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            EMLogger.error("Util", "Error retrieving PrivateKey", e);
            return null;
        }
    }

    public static HashMap<String, Node> loadNodes(String filePath){
        HashMap<String, Node> output = new HashMap<>();
        File file = new File(filePath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<String> lines = new ArrayList<>();
            String line = reader.readLine();
            while(line != null){
                lines.add(line);
                line = reader.readLine();
            }
            int lineCounter = 1;
            for(String entry : lines){
                String[] parts = entry.split("</>");
                try {
                    output.put(parts[0], new Node(parts[1].getBytes(), parts[2].getBytes(), parts[3].substring(0, parts[3].length() - 1).getBytes()));
                }catch (ArrayIndexOutOfBoundsException e){
                    EMLogger.warn("Util", "Error loading Node in line " + lineCounter);
                }
                lineCounter++;
            }
            reader.close();
        } catch (IOException e) {
            EMLogger.error("MessagingClient", "Error loading Nodes from File", e);
        }
        return output;
    }

    public static void saveNodes(HashMap<String, Node> nodes, String filePath){
        File file = new File(filePath);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for(String key : nodes.keySet()){
                byte[] sharedSecret = new byte[0];
                if(nodes.get(key).getSharedSecret() != null){
                    sharedSecret = nodes.get(key).getSharedSecret();
                }
                String line = key + "</>" + new String(nodes.get(key).getPublicKey().getEncoded()) + "</>" + new String(nodes.get(key).getPrivateKey().getEncoded()) + "</>" + new String(sharedSecret) + ".</>";
                writer.write(line);
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            EMLogger.error("MessagingClient", "Error writing Nodes to File", e);
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
        kryo.register(ConnectionVerificationMessage.class);
    }

    public static String encryptString(Node node, String value){
        return new String(node.encrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decryptString(Node node, String value){
        return new String(node.decrypt(value.getBytes(StandardCharsets.UTF_8)));
    }

}
