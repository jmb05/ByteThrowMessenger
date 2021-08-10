package net.jmb19905.server.database;

import net.jmb19905.common.util.Logger;

import java.io.IOException;

public class UserDatabaseManager {

    private static final String DATABASE = "database/users.db";

    public static UserData getUserDataByName(String username){
        UserData userData = null;
        try (UserDataBaseConnection connection = new UserDataBaseConnection(DATABASE)){
            if(connection.hasUser(username)){
                userData = connection.getUserByName(username);
            }
        } catch (IOException e) {
            Logger.log(e, Logger.Level.ERROR);
        }
        return userData;
    }

    public static boolean createUser(String username, String password){
        boolean success = false;
        try (UserDataBaseConnection connection = new UserDataBaseConnection(DATABASE)){
            success = connection.createUser(username, password);
        } catch (IOException e) {
            Logger.log(e, Logger.Level.ERROR);
        }
        return success;
    }

    public static boolean hasUser(String username){
        boolean success = false;
        try (UserDataBaseConnection connection = new UserDataBaseConnection(DATABASE)){
            success = connection.hasUser(username);
        } catch (IOException e) {
            Logger.log(e, Logger.Level.ERROR);
        }
        return success;
    }

    public static record UserData(String username, String password, String salt) {}

}
