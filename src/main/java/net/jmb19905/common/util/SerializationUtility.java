package net.jmb19905.common.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;
import java.util.Base64;

public class SerializationUtility {

    private static final Kryo kryo = new Kryo();

    static {
        registerClass(byte[].class);
    }

    /**
     * Registers a class to Kryo instances from that class can then be converted to binary
     * @param type class type
     * @param <T> type parameter for the class
     */
    public static <T> void registerClass(Class<T> type){
        kryo.register(type);
        Logger.log("Registered " + type + " for FileSerialization", Logger.Level.TRACE);
    }

    /**
     * Converts a Object to binary if that type is registered
     * @param obj the object to be turned into binary
     * @return the binary as byte array
     */
    public static byte[] writeBinary(Object obj){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeObject(output, obj);
        output.flush();
        byte[] out = baos.toByteArray();
        output.close();
        return out;
    }

    /**
     * Reads binary and converts it to the given type
     * @param binary the binary
     * @param type class type
     * @param <T> type parameter for the class
     * @return the instance
     */
    public static <T> T readBinary(byte[] binary, Class<T> type){
        ByteArrayInputStream bais = new ByteArrayInputStream(binary);
        Input input = new Input(bais);
        T out = kryo.readObject(input, type);
        input.close();
        return out;
    }

    /**
     * Converts object into binary file
     * @param file the output file
     * @param obj the object
     * @throws IOException error writing file
     * @throws IllegalArgumentException type not registered
     */
    public static void writeBinaryFile(File file, Object obj) throws IOException, IllegalArgumentException {
        Output output = new Output(new FileOutputStream(file));
        kryo.writeObject(output, obj);
        output.close();
    }

    /**
     * Reads binary from file and converts it into an object
     * @param file the input file
     * @param type class type
     * @param <T> type parameter for the class
     * @return the instance
     * @throws IOException  error reading file
     * @throws IllegalArgumentException type not registered
     */
    public static <T> T readBinaryFile(File file, Class<T> type) throws IOException, IllegalArgumentException {
        Input input = new Input(new FileInputStream(file));
        T out = kryo.readObject(input, type);
        input.close();
        return out;
    }

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
