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

package net.jmb19905.bytethrow.server.database;

import net.jmb19905.bytethrow.common.chat.AbstractChat;
import net.jmb19905.util.Logger;

import java.io.IOException;
import java.util.List;

public class DatabaseManager {

    private static final String DATABASE = "database/users.db";

    private static DatabaseConnection connection;
    private static UserTableHandler userTableHandler;
    private static ChatsTableHandler chatsTableHandler;

    public static void open() {
        connection = new DatabaseConnection(DATABASE);
        connection.addTableHandler(userTableHandler = new UserTableHandler(connection));
        connection.addTableHandler(chatsTableHandler = new ChatsTableHandler(connection));
    }

    public static void close() {
        try {
            connection.close();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public static UserData getUserDataByName(String username) {
        UserData userData = null;
        if (hasUser(username)) {
            userData = userTableHandler.getUserByName(username);
        }
        return userData;
    }

    public static boolean createUser(String username, String password) {
        return userTableHandler.createUser(username, password);
    }

    public static boolean hasUser(String username) {
        return userTableHandler.hasUser(username);
    }

    public static boolean changeUsername(String oldUsername, String newUsername) {
        boolean success = false;
        if (!hasUser(newUsername)) {
            success = userTableHandler.changeUserName(oldUsername, newUsername);
        }
        return success;
    }

    public static boolean changePassword(String username, String password) {
        return userTableHandler.changeUserPassword(username, password);
    }

    public static boolean deleteUser(String name) {
        return userTableHandler.removeUser(name);
    }

    public static record UserData(String username, String password, String salt) {
    }

    public static boolean addChat(AbstractChat chat) {
        return chatsTableHandler.addChat(chat);
    }

    public static boolean hasChat(AbstractChat chat) {
        return chatsTableHandler.hasChat(chat);
    }

    public static AbstractChat getChat(int id) {
        return chatsTableHandler.getChat(id);
    }

    public static AbstractChat getChat(List<String> names) {
        return chatsTableHandler.getChat(names);
    }
}