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

import net.jmb19905.bytethrow.common.User;
import net.jmb19905.bytethrow.common.chat.AbstractChat;
import net.jmb19905.bytethrow.common.chat.PeerChat;
import net.jmb19905.bytethrow.common.util.Util;
import net.jmb19905.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatsTableHandler implements DatabaseConnection.ITableHandler {

    private final Connection connection;

    public ChatsTableHandler(DatabaseConnection databaseConnection) {
        this.connection = databaseConnection.connection;
        try {
            try (Statement stmt = connection.createStatement()) {
                Logger.trace("Opened database successfully");
                String sql = "CREATE TABLE IF NOT EXISTS chats("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "names VARCHAR(40) NOT NULL"
                        + ");";
                stmt.executeUpdate(sql);
            }
        } catch (SQLException ex) {
            Logger.error(ex, "Error opening/creating database/table");
        }
    }

    public boolean addChat(AbstractChat chat) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT into chats (names) VALUES (?);");
            List<String> unsortedNames = new ArrayList<>();
            chat.getMembers().forEach(user -> unsortedNames.add(user.getUsername()));
            List<String> names = Util.sortStringsAlphabetically(unsortedNames);
            StringBuilder builder = new StringBuilder();
            names.forEach(s -> builder.append(s).append(","));
            statement.setString(1, builder.toString());
        } catch (SQLException | NullPointerException e) {
            Logger.error(e, "Error adding user to database");
            return false;
        }
        return true;
    }

    public AbstractChat getChat(int id) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT names FROM chats WHERE id = ?;");
            statement.setInt(1, id);

            String namesString = "";

            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                namesString = resultSet.getString("names");
            }

            String[] names = namesString.split("\\.");

            return new PeerChat(new User(names[0]), new User(names[1]));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AbstractChat getChat(List<String> names) {
        names = Util.sortStringsAlphabetically(names);
        StringBuilder builder = new StringBuilder();
        names.forEach(s -> builder.append(s).append(","));
        String namesString = builder.toString();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM chats WHERE names = ?");
            statement.setString(1, namesString);

            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                return new PeerChat(new User(names.get(0)), new User(names.get(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasChat(AbstractChat chat) {
        List<String> names = new ArrayList<>();
        chat.getMembers().forEach(user -> names.add(user.getUsername()));
        return getChat(names) != null;
    }

}
