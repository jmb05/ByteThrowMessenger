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
import net.jmb19905.bytethrow.server.database.DatabaseManager.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

class UserTableHandler implements DatabaseConnection.ITableHandler {

    private final Connection connection;

    public UserTableHandler(DatabaseConnection databaseConnection) {
        this.connection = databaseConnection.connection;
        try {
            try (Statement stmt = connection.createStatement()) {
                Logger.trace("Opened database successfully");
                String sql = "CREATE TABLE IF NOT EXISTS users("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "username VARCHAR(20) NOT NULL, "
                        + "password VARCHAR(1024) NOT NULL, "
                        + "salt VARCHAR(1024) NOT NULL"
                        + ");";
                stmt.executeUpdate(sql);
            }
        } catch (SQLException ex) {
            Logger.error(ex, "Error opening table");
        }
    }

    /**
     * Adds a user to the users database
     * @param user the userdata of the newly registered user
     * @return if the registration succeeded
     */
    public boolean addUser(UserData user) {
        try {
            assert connection != null;
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username,password,salt) VALUES (?,?,?);");
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.salt());
            statement.execute();
        } catch (SQLException | NullPointerException e) {
            Logger.error( e, "Error adding user to database");
            return false;
        }
        return true;
    }

    public boolean removeUser(String username){
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM table WHERE username = ?;");
            statement.setString(1, username);
            statement.execute();
        }catch (SQLException e){
            Logger.error( e, "Error deleting user from database");
            return false;
        }
        return true;
    }

    /**
     * Gets the data of a user by using the username
     * @param username the username
     * @return the UserSession
     */
    public UserData getUserByName(String username) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT password,salt FROM users WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String password = resultSet.getString("password");
                String salt = resultSet.getString("salt");
                return new UserData(username, password, salt);
            }
        } catch (SQLException | NullPointerException e) {
            Logger.error(e,"Error retrieving user data from database");
        }
        Logger.warn("No UserSession for Username: " + username + " found");
        Logger.trace("Closed database successfully");
        return null;
    }

    /**
     * Creates a new User and saves his data in the user Database
     * @param username the username of the user
     * @param password the password of the user
     * @return if creating the user succeeded
     */
    public boolean createUser(String username, String password) {
        String salt = BCrypt.gensalt();

        UserData userData = new UserData(username, BCrypt.hashpw(password, salt), salt);

        return addUser(userData);
    }

    public boolean changeUserName(String oldUsername, String newUsername){
        if (hasUser(oldUsername)) {
            int id = getUserId(oldUsername);
            if(id >= 0) {
                try {
                    PreparedStatement passwordStatement = connection.prepareStatement("UPDATE users SET username = ? WHERE id = ?");
                    passwordStatement.setString(1, newUsername);
                    passwordStatement.setInt(2, id);
                    passwordStatement.execute();
                    return true;
                } catch (SQLException e) {
                    Logger.warn(e, "Error changing Username");
                }
            }
        }
        return false;
    }

    public boolean changeUserPassword(String username, String newPassword){
        if(hasUser(username)) {
            String salt = BCrypt.gensalt();
            try {
                PreparedStatement passwordStatement = connection.prepareStatement("UPDATE users SET password = ? WHERE username = ?");
                passwordStatement.setString(1, BCrypt.hashpw(newPassword, salt));
                passwordStatement.setString(2, String.valueOf(username));
                passwordStatement.execute();

                PreparedStatement saltStatement = connection.prepareStatement("UPDATE users SET salt = ? WHERE username = ?");
                saltStatement.setString(1, salt);
                saltStatement.setString(2, String.valueOf(username));
                saltStatement.execute();
                return true;
            } catch (SQLException e) {
                Logger.warn(e, "Error changing User Password");
            }
        }
        return false;
    }

    private int getUserId(String username){
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM users WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException | NullPointerException e) {
            Logger.error(e,"Error retrieving user data from database");
        }
        return -1;
    }

    public boolean hasUser(String username){
        return getUserByName(username) != null;
    }
}
