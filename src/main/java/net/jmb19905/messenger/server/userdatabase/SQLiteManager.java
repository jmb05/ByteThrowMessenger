package net.jmb19905.messenger.server.userdatabase;

import net.jmb19905.messenger.util.EMLogger;
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
                EMLogger.trace("SQLiteManager", "Opened database successfully");
                String sql = "CREATE TABLE IF NOT EXISTS users("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "username VARCHAR(20) NOT NULL, "
                        + "password VARCHAR(1024) NOT NULL, "
                        + "salt VARCHAR(1024) NOT NULL,"
                        + "uuid VARCHAR(1024) NOT NULL"
                        + ");";
                stmt.executeUpdate(sql);
                return conn;
            }
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            EMLogger.error("SQLiteManager", "Error opening/creating database/table", ex);
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
            EMLogger.warn("SQLiteManager", "Error adding user to database", e);
            return false;
        }
        EMLogger.trace("SQLiteManager", "Closed database successfully");
        return true;
    }

    /**
     * Gets the data of a user by using the username
     * @param username the username
     * @return the UserData
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
                EMLogger.trace("SQLiteManager", "Closed database successfully");
                return user;
            } else {
                connection.close();
            }
        } catch (SQLException | NullPointerException e) {
            EMLogger.error("SQLiteManager", "Error retrieving user data from database", e);
        }
        EMLogger.trace("SQLiteManager", "No UserData for Username: " + username + " found");
        EMLogger.trace("SQLiteManager", "Closed database successfully");
        return null;
    }

    /**
     * Gets the data of a user by using the User uuid
     * @param uuid the User UUID
     * @return the UserData
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
                EMLogger.trace("SQLiteManager", "Closed database successfully");
                return user;
            }
        } catch (SQLException | NullPointerException e) {
            EMLogger.error("SQLiteManager", "Error retrieving user data from database", e);
        }
        EMLogger.trace("SQLiteManager", "No UserData for User UUID: " + uuid + " found");
        EMLogger.trace("SQLiteManager", "Closed database successfully");
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

    public static class UserData {
        public String username;
        public String password;
        public String salt;
        public UUID uuid;
    }

}
