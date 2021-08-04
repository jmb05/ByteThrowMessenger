package net.jmb19905.common.util;

import java.util.Base64;

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
}
