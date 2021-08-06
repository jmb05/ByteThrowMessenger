package net.jmb19905.common.util;

import java.io.*;
import java.util.*;

public class SerializationUtility {

    /**
     * Encodes binary to String
     * @param binary binary to be converted
     * @return the output String
     */
    public static String encodeBinary(byte[] binary){
        return Base64.getEncoder().encodeToString(binary);
    }

    /**
     * Decodes a String back to binary
     * @param binaryAsString String to be converted
     * @return the output binary as byte-array
     */
    public static byte[] decodeBinary(String binaryAsString){
        return Base64.getDecoder().decode(binaryAsString);
    }

    public static byte[] loadFile(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            try (InputStream inputStream = new FileInputStream(file)){
                return inputStream.readAllBytes();
            } catch (IOException e) {
                Logger.log(e, Logger.Level.WARN);
            }
        }else{
            Logger.log("File doesn't exist", Logger.Level.WARN);
        }
        return new byte[0];
    }

    public static byte[][] chunkArray(byte[] arrayIn, int intervalLength){
        List<byte[]> data = new ArrayList<>();
        int arrayLength = arrayIn.length;
        double d = ((double) arrayLength) / intervalLength;
        for(int i=0;i<((int) Math.ceil(d));i++){
            int startPoint = i * intervalLength;
            data.add(Arrays.copyOfRange(arrayIn, startPoint, startPoint + intervalLength));
        }
        byte[][] output = new byte[data.size()][intervalLength];
        for(int i=0;i<data.size();i++){
            output[i] = data.get(i);
        }
        return output;
    }

}
