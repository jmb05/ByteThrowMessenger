package net.jmb19905.messenger.client;

public class UserSession {

    public String username;
    public String password;

    public boolean loggedIn = false;

    public boolean isInitialized() {
        if (username == null || password == null){
            return false;
        }
        return !username.equals("") && !password.equals("");
    }

}
