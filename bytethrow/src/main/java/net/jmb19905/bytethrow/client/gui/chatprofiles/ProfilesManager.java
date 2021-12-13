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

package net.jmb19905.bytethrow.client.gui.chatprofiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfilesManager {

    private static final List<IChatProfile> profiles = new ArrayList<>();

    public static void addProfile(IChatProfile profile){
        profiles.add(profile);
    }

    public static void removeProfile(IChatProfile profile){
        profiles.remove(profile);
    }

    public static IChatProfile getProfileByID(UUID id){
        return profiles.stream().filter(p -> p.getUniqueID().equals(id)).findFirst().orElse(null);
    }

    public static List<IChatProfile> getProfilesByName(String name){
        return profiles.stream().filter(p -> p.getDisplayName().equals(name)).collect(Collectors.toList());
    }

    public static void clear(){
        profiles.clear();
    }

}
