/*
 * Copyright (c) $ Jared M. Bennett today.year. Please refer to LICENSE.txt
 */

package net.jmb19905.bytethrow.common.packets;

import io.netty.channel.Channel;
import net.jmb19905.bytethrow.common.util.NetworkingUtility;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.utility.NetworkUtility;

public class PacketManager {

    public static void sendChangeUsername(String name, Channel channel, Encryption encryption){
        ChangeUserDataPacket packet = new ChangeUserDataPacket();
        packet.type = "username";
        packet.value = name;
        NetworkUtility.sendTcp(channel, packet, encryption);
    }

    public static void sendChangePassword(String password, Channel channel, Encryption encryption){
        ChangeUserDataPacket packet = new ChangeUserDataPacket();
        packet.type = "password";
        packet.value = password;
        NetworkUtility.sendTcp(channel, packet, encryption);
    }

    public static void confirmIdentity(String username, String password, Channel channel, Encryption encryption){
        LoginPacket loginPacket = new LoginPacket();
        loginPacket.name = username;
        loginPacket.password = password;
        loginPacket.confirmIdentity = true;

        NetworkingUtility.sendPacket(loginPacket, channel, encryption);
    }

}
