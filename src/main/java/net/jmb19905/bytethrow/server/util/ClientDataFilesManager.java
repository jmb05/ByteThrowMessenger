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

package net.jmb19905.bytethrow.server.util;

import net.jmb19905.bytethrow.common.chat.Chat;
import net.jmb19905.bytethrow.common.serial.ChatSerial;
import net.jmb19905.bytethrow.server.StartServer;

public class ClientDataFilesManager {

    public static void loadChats(){
        StartServer.manager.setChats(ChatSerial.readAllChats());
    }

    public static void writeChats(String name){
        for(Chat chat : StartServer.manager.getChats(name)){
            ChatSerial.write(chat);
        }
    }

    public static void writeChats(){
        for(Chat chat : StartServer.manager.getChats()){
            ChatSerial.write(chat);
        }
    }

}
