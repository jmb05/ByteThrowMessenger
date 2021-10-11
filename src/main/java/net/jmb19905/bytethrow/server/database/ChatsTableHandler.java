/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.server.database;

import net.jmb19905.bytethrow.common.Chat;
import net.jmb19905.bytethrow.common.util.Util;
import net.jmb19905.util.Logger;

import java.sql.*;
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

    public boolean addChat(Chat chat) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT into chats (names) VALUES (?);");
            List<String> names = Util.sortStringsAlphabetically(chat.getClients());
            StringBuilder builder = new StringBuilder();
            names.forEach(s -> builder.append(s).append(","));
            statement.setString(1, builder.toString());
        } catch (SQLException | NullPointerException e) {
            Logger.error(e, "Error adding user to database");
            return false;
        }
        return true;
    }

    public Chat getChat(int id) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT names FROM chats WHERE id = ?;");
            statement.setInt(1, id);

            String namesString = "";

            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                namesString = resultSet.getString("names");
            }

            String[] names = namesString.split("\\.");

            Chat chat = new Chat();
            chat.addClients(List.of(names));

            return chat;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Chat getChat(List<String> names) {
        names = Util.sortStringsAlphabetically(names);
        StringBuilder builder = new StringBuilder();
        names.forEach(s -> builder.append(s).append(","));
        String namesString = builder.toString();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM chats WHERE names = ?");
            statement.setString(1, namesString);

            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                Chat chat = new Chat();
                chat.addClients(names);
                return chat;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasChat(Chat chat){
        return getChat(chat.getClients()) != null;
    }

}
