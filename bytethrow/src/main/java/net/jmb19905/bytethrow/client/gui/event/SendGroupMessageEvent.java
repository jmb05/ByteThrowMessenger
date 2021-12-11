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

package net.jmb19905.bytethrow.client.gui.event;

import net.jmb19905.bytethrow.client.gui.chatprofiles.GroupChatProfile;
import net.jmb19905.bytethrow.common.chat.GroupMessage;
import org.jetbrains.annotations.NotNull;

public class SendGroupMessageEvent extends SendMessageEvent {

    public static final String ID = "send_group_message";

    private final GroupMessage groupMessage;
    private final GroupChatProfile chatProfile;

    public SendGroupMessageEvent(@NotNull GuiEventContext ctx, GroupMessage groupMessage, GroupChatProfile chatProfile) {
        super(ctx, ID, groupMessage.getMessage());
        this.groupMessage = groupMessage;
        this.chatProfile = chatProfile;
    }

    public GroupMessage getGroupMessage() {
        return groupMessage;
    }

    public GroupChatProfile getChatProfile() {
        return chatProfile;
    }
}
