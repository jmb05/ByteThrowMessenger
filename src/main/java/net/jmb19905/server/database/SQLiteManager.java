package net.jmb19905.server.database;

import net.jmb19905.common.util.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class SQLiteManager {

    /**
     * Opens a SQLite Database
     * @param fileName the filename of the Database
     * @return the Connection to the database
     */
    public static Connection connect(String fileName) {
        try {
            File database = new File(fileName);
            if (!database.exists()) {
                database.getParentFile().mkdirs();
                database.createNewFile();
            }
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + fileName);
            try (Statement stmt = conn.createStatement()) {
                Logger.log("Opened database successfully", Logger.Level.TRACE);
                String sql = "CREATE TABLE IF NOT EXISTS users("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "username VARCHAR(20) NOT NULL, "
                        + "password VARCHAR(1024) NOT NULL, "
                        + "salt VARCHAR(1024) NOT NULL,"
                        + "uuid VARCHAR(1024)"
                        + ");";
                stmt.executeUpdate(sql);
                return conn;
            }
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            Logger.log(ex, "Error opening/creating database/table", Logger.Level.ERROR);
            return null;
        }
    }

    /**
     * Adds a user to the users database
     * @param user the userdata of the newly registered user
     * @return if the registration succeeded
     */
    public static boolean addUser(UserData user) {
        Connection connection = connect("database/users.db");
        try {
            assert connection != null;
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username,password,salt,uuid) VALUES (?,?,?,?);");
            statement.setString(1, user.username);
            statement.setString(2, user.password);
            statement.setString(3, user.salt);
            statement.setString(4, user.uuid.toString());
            statement.execute();
            connection.close();
        } catch (SQLException | NullPointerException e) {
            Logger.log( e, "Error adding user to database", Logger.Level.ERROR);
            return false;
        }
        Logger.log("Closed database successfully", Logger.Level.TRACE);
        return true;
    }

    public static boolean addUser(String username, String password, String salt){
        Connection connection = connect("database/users.db");
        try {
            assert connection != null;
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username,password,salt,uuid) VALUES (?,?,?,?);");
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, salt);
            statement.setString(4, null);
            statement.execute();
            connection.close();
        } catch (SQLException | NullPointerException e) {
            Logger.log( e, "Error adding user to database", Logger.Level.ERROR);
            return false;
        }
        Logger.log("Closed database successfully", Logger.Level.TRACE);
        return true;
    }

    /**
     * Gets the data of a user by using the username
     * @param username the username
     * @return the UserSession
     */
    public static UserData getUserByName(String username) {
        Connection connection = connect("database/users.db");
        try {
            assert connection != null;
            PreparedStatement statement = connection.prepareStatement("SELECT password,salt,uuid FROM users WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String password = resultSet.getString("password");
                String salt = resultSet.getString("salt");
                String uuidAsString = resultSet.getString("uuid");
                UserData user = new UserData();
                user.username = username;
                user.password = password;
                user.salt = salt;
                user.uuid = UUID.fromString(uuidAsString);
                connection.close();
                Logger.log("Closed database successfully", Logger.Level.TRACE);
                return user;
            } else {
                connection.close();
            }
        } catch (SQLException | NullPointerException e) {
            Logger.log(e,"Error retrieving user data from database", Logger.Level.ERROR);
        }
        Logger.log("No UserSession for Username: " + username + " found", Logger.Level.WARN);
        Logger.log("Closed database successfully", Logger.Level.TRACE);
        return null;
    }

    /**
     * Gets the data of a user by using the User uuid
     * @param uuid the User UUID
     * @return the UserSession
     */
    @Deprecated
    public static UserData getUserByID(UUID uuid) {
        Connection connection = connect("database/users.db");
        try {
            assert connection != null;
            PreparedStatement statement = connection.prepareStatement("SELECT username,password,salt FROM users WHERE uuid = ?");
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String salt = resultSet.getString("salt");
                UserData user = new UserData();
                user.username = username;
                user.password = password;
                user.salt = salt;
                user.uuid = uuid;
                connection.close();
                Logger.log("Closed database successfully", Logger.Level.TRACE);
                return user;
            }
        } catch (SQLException | NullPointerException e) {
            Logger.log(e, "Error retrieving user data from database", Logger.Level.ERROR);
        }
        Logger.log("No UserSession for User UUID: " + uuid + " found", Logger.Level.WARN);
        Logger.log("Closed database successfully", Logger.Level.TRACE);
        return null;
    }

    /**
     * Creates a new User and saves his data in the user Database
     * @param username the username of the user
     * @param password the password of the user
     * @return the UUID of the User
     */
    public static UUID createUser(String username, String password) {
        String salt = BCrypt.gensalt();
        UUID uuid = UUID.randomUUID();

        SQLiteManager.UserData userData = new SQLiteManager.UserData();
        userData.username = username;
        userData.salt = salt;
        userData.password = BCrypt.hashpw(password, salt);
        userData.uuid = uuid;

        if (SQLiteManager.addUser(userData)) {
            return uuid;
        }
        return null;
    }

    public static boolean changeUserName(String oldUsername, String newUsername){
        Connection databaseConnection = connect("database/users.db");
        try {
            UserData userData = getUserByName(oldUsername);
            if(addUser(newUsername, userData.password, userData.salt)){
                PreparedStatement passwordStatement = databaseConnection.prepareStatement("DELETE FROM users WHERE username = ?");
                passwordStatement.setString(1, oldUsername);
                passwordStatement.execute();
                return true;
            }else{
                Logger.log("Error changing Username", Logger.Level.WARN);
                return false;
            }
        }catch (SQLException e){
            Logger.log(e, "Error changing Username", Logger.Level.WARN);
            return false;
        }
    }

    public static boolean changeUserPassword(String username, String newPassword){
        String salt = BCrypt.gensalt();
        Connection databaseConnection = connect("database/users.db");
        try {
            PreparedStatement passwordStatement = databaseConnection.prepareStatement("UPDATE users SET password = ? WHERE username = ?");
            passwordStatement.setString(1, newPassword);
            passwordStatement.setString(2, String.valueOf(username));
            passwordStatement.execute();

            PreparedStatement saltStatement = databaseConnection.prepareStatement("UPDATE users SET salt = ? WHERE username = ?");
            saltStatement.setString(1, salt);
            saltStatement.setString(2, String.valueOf(username));
            saltStatement.execute();
            return true;
        } catch (SQLException e) {
            Logger.log(e, "Error changing User Password", Logger.Level.WARN);
            return false;
        }
    }

    public static class UserData {
        public String username;
        public String password;
        public String salt;
        public UUID uuid;
    }

}
