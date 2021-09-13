package net.jmb19905.client.util;

import net.jmb19905.client.StartClient;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.ResourceUtility;

import java.util.*;

public class Localisation {

    private static ResourceBundle resourceBundle;

    public static void reload(){
        String[] localeParts = StartClient.config.lang.split("_");
        Locale locale = new Locale(localeParts[0], localeParts[1]);
        resourceBundle = ResourceBundle.getBundle("lang.bundle", locale);
    }

    public static String[] getLocales(){
        String[] locales = ResourceUtility.getResourceFiles("lang");
        for(int i=0;i<locales.length;i++){
            locales[i] = locales[i].replace("bundle_", "").replace(".properties", "");
        }
        return locales;
    }

    public static String get(String key){
        try {
            return resourceBundle.getString(key);
        }catch (MissingResourceException e){
            Logger.log(e, Logger.Level.WARN);
            return key;
        }
    }

    public static String get(String key, String input){
        return get(key).replaceAll("~", input);
    }

}
