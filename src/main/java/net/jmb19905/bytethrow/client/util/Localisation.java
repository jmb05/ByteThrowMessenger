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

package net.jmb19905.bytethrow.client.util;

import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.common.util.ResourceUtility;
import net.jmb19905.util.Logger;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localisation {

    private static ResourceBundle resourceBundle;

    public static void reload() {
        String[] localeParts = StartClient.config.lang.split("_");
        Locale locale = new Locale(localeParts[0], localeParts[1]);
        resourceBundle = ResourceBundle.getBundle("lang.bundle", locale);
    }

    public static String[] getLocales() {
        String[] locales = ResourceUtility.getResourceFiles("lang");
        for (int i = 0; i < locales.length; i++) {
            locales[i] = locales[i].replace("bundle_", "").replace(".properties", "");
        }
        return locales;
    }

    public static String get(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            Logger.warn(e);
            return key;
        }
    }

    public static String get(String key, String input) {
        return get(key).replaceAll("~", input);
    }

}
