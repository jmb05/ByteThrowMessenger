/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
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

    protected DatabaseConnection(String filename){
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

    public void addTableHandler(ITableHandler tableHandler){
        tableHandlers.add(tableHandler);
    }

    public List<ITableHandler> getTableHandlers() {
        return tableHandlers;
    }

    @Override
    public void close() throws IOException {
        if(connection != null){
            try {
                connection.close();
                Logger.trace("Closed database successfully");
            } catch (SQLException e) {
                throw new IOException("Error closing DataBase Connection");
            }
        }
    }

    public interface ITableHandler{ }
}
