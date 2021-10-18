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

import net.jmb19905.util.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection implements Closeable {

    protected Connection connection = null;
    private final List<ITableHandler> tableHandlers = new ArrayList<>();

    protected DatabaseConnection(String filename) {
        try {
            File database = new File(filename);
            if (!database.exists()) {
                database.getParentFile().mkdirs();
                database.createNewFile();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            Logger.error(ex, "Error opening/creating database");
        }
    }

    public void addTableHandler(ITableHandler tableHandler) {
        tableHandlers.add(tableHandler);
    }

    public List<ITableHandler> getTableHandlers() {
        return tableHandlers;
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            try {
                connection.close();
                Logger.trace("Closed database successfully");
            } catch (SQLException e) {
                throw new IOException("Error closing DataBase Connection");
            }
        }
    }

    public interface ITableHandler {
    }
}
