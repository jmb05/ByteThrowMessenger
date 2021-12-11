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

package net.jmb19905.bytethrow.common.chat;

import net.jmb19905.bytethrow.common.User;

import java.util.*;

public abstract class AbstractChat implements IChat{

    protected UUID uniqueId;

    /**
     * All the clients that participate in this chat.
     */
    protected List<User> members = new ArrayList<>();

    public AbstractChat() {
        uniqueId = UUID.randomUUID();
    }

    public AbstractChat(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }


    public void addClient(User name) {
        if (!members.contains(name)) members.add(name);
    }

    public boolean removeClient(User name){
        boolean f = members.contains(name);
        members.remove(name);

        return f;
    }

    public void addClients(List<User> users) {
        users.stream().filter(user -> !members.contains(user)).forEach(user -> members.add(user));
    }

    public boolean hasClient(User user) {
        return members.contains(user);
    }

    @Override
    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    public boolean equivalent(AbstractChat o) {
        if (this == o) return true;
        if (o == null) return false;
        return listEqualsIgnoreOrder(members, o.members) && Objects.equals(uniqueId, o.uniqueId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractChat chat = (AbstractChat) o;
        return listEqualsIgnoreOrder(members, chat.members) && Objects.equals(uniqueId, chat.uniqueId);
    }

    public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

    @Override
    public int hashCode() {
        return Objects.hash(members, uniqueId);
    }

    @Override
    public String toString() {
        return "AbstractChat{" +
                "clients=" + members +
                ", uniqueId=" + uniqueId +
                '}';
    }

    public boolean clientsEquals(AbstractChat chat) {
        return chat.getMembers().containsAll(members) && members.containsAll(chat.getMembers());
    }
}