package net.jmb19905.messenger.util;

import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FormattedImage {

    public String name;
    public BufferedImage image;
    public String format;

    public FormattedImage(String name, String format, BufferedImage image){
        this.name = name;
        this.image = image;
        this.format = format;
    }

    public void writeWithNewName(File file, boolean overwriteExisting) throws IOException {
        if(file.exists() && overwriteExisting){
            BTMLogger.warn("FormattedImage", "Cannot write Image - file already exists");
            return;
        }
        if(file.isDirectory()){
            BTMLogger.warn("FormattedImage", "Cannot write Image - file is a directory");
        }
        FileUtility.createFile(file);
        ImageIO.write(image, format, new File(file.getAbsolutePath()));

    }

    public void write(File directory, boolean overwriteExisting) throws IOException {
        writeWithNewName(new File(directory.getAbsolutePath() + "/" + name + "." + format), overwriteExisting);
    }

    public static FormattedImage read(File file) throws IOException {
        String rawName = file.getName();
        String[] parts = rawName.split("\\.");
        StringBuilder nameBuilder = new StringBuilder();
        String format = "";
        for(int i=0;i<parts.length;i++){
            if(i < parts.length - 1){
                nameBuilder.append(parts[i]);
            }else{
                format = parts[i];
            }
        }
        return new FormattedImage(nameBuilder.toString(), format, ImageIO.read(file));
    }

    @Override
    public String toString() {
        return name + "." + format;
    }
}
