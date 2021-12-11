/*
 * A simple Messenger written in Java
 * Copyright (C) 2020-2021  Jared M. Bennett
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.jmb19905.bytethrow.common;

import org.jetbrains.annotations.Nullable;

public class User implements Cloneable{

    private String username;
    private String password = "";
    private long avatarSeed = -1;

    public User(String username) {
        this.username = username;
    }

    public User(String username, String password) {
        this(username);
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAvatarSeed(long avatarSeed) {
        this.avatarSeed = avatarSeed;
    }

    public String getUsername() {
        return username;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public long getAvatarSeed() {
        return avatarSeed;
    }

    public String deconstruct() {
        return "User{" + username + "//" + password + "//" + avatarSeed + "}";
    }

    public void removePassword() {
        setPassword("");
    }

    @Nullable
    public static User constructUser(String data) {
        if(!data.startsWith("User")) return null;
        data = data.replaceAll("User", "");
        data = data.replaceAll("\\{", "");
        data = data.replaceAll("}", "");
        String[] parts = data.split("//");
        User user = new User(parts[0], parts[1]);
        user.setAvatarSeed(Long.parseLong(parts[2]));
        return user;
    }

    @Override
    public User clone() {
        try {
            User user = (User) super.clone();
            user.setUsername(getUsername());
            user.setPassword(getPassword());
            user.setAvatarSeed(getAvatarSeed());
            return user;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Deprecated
    @Override
    public String toString() {
        return deconstruct();
    }

    public String toSafeString() {
        return "User{" + username + "////" + avatarSeed + "}";
    }

    public boolean equals(Object user) {
        if(!(user instanceof User) || user == null) return false;
        return username.equals(((User) user).getUsername());
    }
}
