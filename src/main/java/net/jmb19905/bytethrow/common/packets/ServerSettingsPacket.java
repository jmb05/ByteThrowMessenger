/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets;

import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.common.packets.registry.PacketRegistry;

import java.nio.charset.StandardCharsets;

public class ServerSettingsPacket extends Packet {

    private static final String ID = "server_settings";

    public boolean securePasswords = true;

    public ServerSettingsPacket() {
        super(PacketRegistry.getInstance().getPacketType(ID));
    }

    @Override
    public void construct(String[] data) {
        securePasswords = Boolean.parseBoolean(data[1]);
    }

    @Override
    public byte[] deconstruct() {
        String data = ID + "|" + securePasswords;
        return data.getBytes(StandardCharsets.UTF_8);
    }
}
