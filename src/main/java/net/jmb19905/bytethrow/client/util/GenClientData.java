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

import net.jmb19905.bytethrow.common.util.ConfigManager;

import java.io.File;

public class GenClientData {

    public static void main(String[] args) {
        System.out.print("Generating Log Directory...   ");
        System.out.print((genLogDir() ? "Success" : "Failed") + "\n");
        System.out.print("Generating UserData Directory...  ");
        System.out.print((genUserDataDir() ? "Success" : "Failed") + "\n");
        System.out.print("Generating Config...  ");
        System.out.print((genConfig() ? "Success" : "Failed") + "\n");
    }

    public static boolean genLogDir(){
        File logDir = new File("logs");
        if(!logDir.exists()) {
            return logDir.mkdirs();
        }
        return true;
    }

    public static boolean genUserDataDir(){
        File userDataDir = new File("userdata");
        if(!userDataDir.exists()) {
            return userDataDir.mkdirs();
        }
        return true;
    }

    public static boolean genConfig(){
        String configFilePath = ConfigManager.getConfigPath() + "client_config.json";
        File configFile = new File(configFilePath);
        if(!configFile.exists()) {
            return ConfigManager.saveClientConfig(new ConfigManager.ClientConfig(), configFilePath);
        }
        return true;
    }

}
