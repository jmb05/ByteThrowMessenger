package net.jmb19905.client.util;

import net.jmb19905.client.ClientMain;
import net.jmb19905.common.util.Logger;
import net.jmb19905.common.util.ResourceUtility;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localisation {

    private static ResourceBundle resourceBundle;

    public static void reload(){
        String[] localeParts = ClientMain.config.lang.split("_");
        Locale locale = new Locale(localeParts[0], localeParts[1]);
        resourceBundle = ResourceBundle.getBundle("lang.bundle", locale);
    }

    public static String[] getLocales(){
        List<String> locales = ResourceUtility.getResourceFiles("lang");
        for(int i=0;i<locales.size();i++){
            locales.set(i, locales.get(i).replace("bundle_", "").replace(".properties", ""));
        }
        return locales.toArray(new String[0]);
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
