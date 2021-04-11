package net.jmb19905.messenger.util;

import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.util.config.ConfigManager;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Holds some Variables
 */
public class Variables {

    public static final String startupTime = new SimpleDateFormat("yyyy-MM.dd-HH-mm-ss").format(new Date());
    public static String currentSide = "";

    public static String os = System.getProperty("os.name");
    public static String dataDirectory = os.toLowerCase(Locale.ROOT).contains("linux") ? System.getProperty("user.home") + "/.btm/" : System.getenv("APPDATA") + "/btm/";

    public static Font defaultFont;
    public static Font italicFont;
    public static Font boldFont;

    public static void initFonts(){
        ConfigManager.ClientConfig config = ByteThrowClient.clientConfig;
        defaultFont = new Font(config.font, Font.PLAIN, config.fontSize);
        italicFont = new Font(config.font, Font.ITALIC, config.fontSize);
        boldFont = new Font(config.font, Font.BOLD, config.fontSize);
    }

}
