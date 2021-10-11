package net.jmb19905.bytethrow.client.util;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.util.ResourceUtility;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {

    private static final Map<String, ImageIcon> iconPack = new HashMap<>();
    private static boolean dark;

    public static void init(){
        String themeStringID = StartClient.config.theme;
        switch (themeStringID) {
            case "Darcula" -> {
                FlatDarculaLaf.setup();
                dark = true;
            }
            case "Dark" -> {
                FlatDarkLaf.setup();
                dark = true;
            }
            case "Light" -> {
                FlatLightLaf.setup();
                dark = false;
            }
            case "IntelliJ" -> {
                FlatIntelliJLaf.setup();
                dark = false;
            }
        }
        initIcons();
    }

    private static void initIcons(){
        String pathAddition = dark ? "" : "_dark";
        iconPack.put("settings_wheel", new ImageIcon(ResourceUtility.getImageResource("icons/settings_wheel" + pathAddition + ".png")));
        iconPack.put("send", new ImageIcon(ResourceUtility.getImageResource("icons/send" + pathAddition + ".png")));
    }

    public static ImageIcon getIcon(String id){
        return iconPack.get(id);
    }

}
