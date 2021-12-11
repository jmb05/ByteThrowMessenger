/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.common.util;

import net.jmb19905.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class SerializationUtility {

    /**
     * Encodes binary to String
     *
     * @param binary binary to be converted
     * @return the output String
     */
    public static String encodeBinary(byte[] binary) {
        return Base64.getEncoder().encodeToString(binary);
    }

    /**
     * Decodes a String back to binary
     *
     * @param binaryAsString String to be converted
     * @return the output binary as byte-array
     */
    public static byte[] decodeBinary(String binaryAsString) {
        return Base64.getDecoder().decode(binaryAsString);
    }

    public static byte[] loadFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                return inputStream.readAllBytes();
            } catch (IOException e) {
                Logger.warn(e);
            }
        } else {
            Logger.warn("File doesn't exist");
        }
        return new byte[0];
    }

    public static byte[][] chunkArray(byte[] arrayIn, int intervalLength) {
        List<byte[]> data = new ArrayList<>();
        int arrayLength = arrayIn.length;
        double d = ((double) arrayLength) / intervalLength;
        for (int i = 0; i < ((int) Math.ceil(d)); i++) {
            int startPoint = i * intervalLength;
            data.add(Arrays.copyOfRange(arrayIn, startPoint, startPoint + intervalLength));
        }
        byte[][] output = new byte[data.size()][intervalLength];
        for (int i = 0; i < data.size(); i++) {
            output[i] = data.get(i);
        }
        return output;
    }

}
