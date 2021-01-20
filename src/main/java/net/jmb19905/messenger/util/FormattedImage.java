package net.jmb19905.messenger.util;

import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FormattedImage {

    public String name;
    public BufferedImage image;
    public String format;

    public FormattedImage(String name, BufferedImage image, String format){
        this.name = name;
        this.image = image;
        this.format = format;
    }

    public void write(File directory) throws IOException {
        if(!directory.isDirectory()){
            directory = directory.getParentFile();
        }if(!directory.exists()){
            directory.mkdirs();
        }
        ImageIO.write(image, format, new File(directory.getAbsolutePath() + "/" + name + "." + format));
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
        return new FormattedImage(nameBuilder.toString(), ImageIO.read(file), format);
    }

    @Override
    public String toString() {
        return name + "." + format;
    }
}
