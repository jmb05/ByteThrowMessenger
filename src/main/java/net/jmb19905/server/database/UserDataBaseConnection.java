package net.jmb19905.server.database;

import net.jmb19905.common.util.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.*;

class UserDataBaseConnection implements Closeable {

    private Connection connection = null;

    /**
     * Opens a SQLite Database
     * @param fileName the filename of the Database
     */
    protected UserDataBaseConnection(String fileName) {
        try {
            File database = new File(fileName);
            if (!database.exists()) {
                database.getParentFile().mkdirs();
                database.createNewFile();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
            try (Statement stmt = connection.createStatement()) {
                Logger.log("Opened database successfully", Logger.Level.TRACE);
                String sql = "CREATE TABLE IF NOT EXISTS users("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "username VARCHAR(20) NOT NULL, "
                        + "password VARCHAR(1024) NOT NULL, "
                        + "salt VARCHAR(1024) NOT NULL"
                        + ");";
                stmt.executeUpdate(sql);
            }
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            Logger.log(ex, "Error opening/creating database/table", Logger.Level.ERROR);
        }
    }

    /**
     * Adds a user to the users database
     * @param user the userdata of the newly registered user
     * @return if the registration succeeded
     */
    public boolean addUser(UserDatabaseManager.UserData user) {
        try {
            assert connection != null;
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username,password,salt) VALUES (?,?,?);");
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.salt());
            statement.execute();
        } catch (SQLException | NullPointerException e) {
            Logger.log( e, "Error adding user to database", Logger.Level.ERROR);
            return false;
        }
        Logger.log("Closed database successfully", Logger.Level.TRACE);
        return true;
    }

    private boolean addUser(String username, String password, String salt){
        try {
            assert connection != null;
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username,password,salt) VALUES (?,?,?);");
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, salt);
            statement.execute();
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
    public UserDatabaseManager.UserData getUserByName(String username) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT password,salt FROM users WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String password = resultSet.getString("password");
                String salt = resultSet.getString("salt");
                return new UserDatabaseManager.UserData(username, password, salt);
            }
        } catch (SQLException | NullPointerException e) {
            Logger.log(e,"Error retrieving user data from database", Logger.Level.ERROR);
        }
        Logger.log("No UserSession for Username: " + username + " found", Logger.Level.WARN);
        Logger.log("Closed database successfully", Logger.Level.TRACE);
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

        UserDatabaseManager.UserData userData = new UserDatabaseManager.UserData(username, BCrypt.hashpw(password, salt), salt);

        return addUser(userData);
    }

    public boolean changeUserData(String oldUsername, UserDatabaseManager.UserData userData){
        if (hasUser(oldUsername)) {
            UserDatabaseManager.UserData oldUserData = getUserByName(oldUsername);
            try {
                PreparedStatement deleteUserStatement = connection.prepareStatement("DELETE FROM users WHERE username = ?");
                deleteUserStatement.setString(1, oldUsername);
                deleteUserStatement.execute();

                addUser(userData);
                return true;
            } catch (SQLException e) {
                Logger.log(e, "Error changing Username", Logger.Level.WARN);
                if (!hasUser(oldUsername)){
                    addUser(oldUserData);
                }
            }
        }
        return false;
    }

    public boolean changeUserName(String oldUsername, String newUsername){
        if (hasUser(oldUsername)) {
            int id = getId(oldUsername);
            if(id >= 0) {
                try {
                    PreparedStatement passwordStatement = connection.prepareStatement("UPDATE users SET username = ? WHERE id = ?");
                    passwordStatement.setString(1, newUsername);
                    passwordStatement.setInt(2, id);
                    passwordStatement.execute();
                    return true;
                } catch (SQLException e) {
                    Logger.log(e, "Error changing Username", Logger.Level.WARN);
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
                passwordStatement.setString(1, newPassword);
                passwordStatement.setString(2, String.valueOf(username));
                passwordStatement.execute();

                PreparedStatement saltStatement = connection.prepareStatement("UPDATE users SET salt = ? WHERE username = ?");
                saltStatement.setString(1, salt);
                saltStatement.setString(2, String.valueOf(username));
                saltStatement.execute();
                return true;
            } catch (SQLException e) {
                Logger.log(e, "Error changing User Password", Logger.Level.WARN);
            }
        }
        return false;
    }

    private int getId(String username){
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM users WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("password");
            }
        } catch (SQLException | NullPointerException e) {
            Logger.log(e,"Error retrieving user data from database", Logger.Level.ERROR);
        }
        return -1;
    }

    public boolean hasUser(String username){
        return getUserByName(username) != null;
    }

    @Override
    public void close() throws IOException{
        if(connection != null){
            try {
                connection.close();
                Logger.log("Closed database successfully", Logger.Level.TRACE);
            } catch (SQLException e) {
                throw new IOException("Error closing DataBase Connection");
            }
        }
    }
}
