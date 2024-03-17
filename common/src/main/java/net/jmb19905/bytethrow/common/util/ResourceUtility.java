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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceUtility {

    public static String readWhole(String s) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResource(s)))) {
            return reader.lines().reduce((s1, s2) -> s1 + s2).orElse("");
        } catch (IOException e) {
            Logger.error(e);
            return "";
        }
    }

    public static Properties readResourceProperties(String s) {
        Properties prop = new Properties();
        try {
            InputStream stream = getResource(s);
            prop.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * Loads an image from the classpath
     *
     * @param s the path of the image
     * @return the loaded image
     */
    public static BufferedImage getImageResource(String s) {
        try {
            InputStream stream = getResource(s);
            return ImageIO.read(stream);
        } catch (IOException e) {
            Logger.warn(e, "Error loading image");
            return null;
        }
    }

    /**
     * Gets a Resource as a Stream
     *
     * @param s path for the resource
     * @return the stream
     */
    public static InputStream getResource(String s) {
        return ResourceUtility.class.getClassLoader().getResourceAsStream(s);
    }

    public static URL getResourceAsURL(String s) {
        return ResourceUtility.class.getClassLoader().getResource(s);
    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @return Just the name of each member item, not the full paths.
     * @author Greg Briggs - taken from: <a href="http://www.uofr.net/~greg/java/get-resource-listing.html">http://www.uofr.net/~greg/java/get-resource-listing.html</a>
     */
    public static String[] getResourceFiles(String path) {
        URL dirURL = getResourceAsURL(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            try {
                return new File(dirURL.toURI()).list();
            } catch (URISyntaxException e) {
                Logger.error(e);
            }
        }

        if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = ResourceUtility.class.getName().replace(".", "/") + ".class";
            dirURL = getResourceAsURL(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = null;
            try {
                jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
            } catch (IOException e) {
                Logger.error(e);
            }
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length() + 1);
                    if (!entry.equals("")) {
                        int checkSubdir = entry.indexOf("/");
                        if (checkSubdir >= 0) {
                            // if it is a subdirectory, we just return the directory name
                            entry = entry.substring(0, checkSubdir);
                        }
                        result.add(entry);
                    }
                }
            }
            return result.toArray(new String[0]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }
}
