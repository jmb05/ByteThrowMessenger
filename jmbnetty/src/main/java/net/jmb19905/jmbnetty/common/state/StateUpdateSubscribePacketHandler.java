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

package net.jmb19905.jmbnetty.common.state;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.jmbnetty.common.packets.handler.PacketHandler;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.jmbnetty.utility.NetworkUtility;
import net.jmb19905.util.crypto.Encryption;

public class StateUpdateSubscribePacketHandler extends PacketHandler<StateUpdateSubscribePacket> {
    @Override
    public void handle(ChannelHandlerContext ctx, StateUpdateSubscribePacket packet) {
        TcpServerHandler serverHandler = (TcpServerHandler) ctx.handler();
        if(packet.register) {
            serverHandler.addStateChangeListener(evt -> {
                String stateName = evt.getStateName();
                String stateId = evt.getStateTypeId();
                if (stateName.equals(packet.stateName) && stateId.equals(packet.stateId) && !ctx.isRemoved()) {
                    StateType<? extends State, ? extends StateChangePacket<? extends State>> stateType = StateRegistry.getInstance().getStateType(evt.getStateTypeId());
                    sendStateChangePacket(ctx, stateType, stateName, serverHandler.getEncryption());
                }
            });
            sendStateChangePacket(ctx, StateRegistry.getInstance().getStateType(packet.stateId), packet.stateName, serverHandler.getEncryption());
        }else if(serverHandler.getStateManager().hasState(packet.stateName)){
            serverHandler.getStateManager().removeState(packet.stateName);
        }
    }

    private void sendStateChangePacket(ChannelHandlerContext ctx, StateType<? extends State, ? extends StateChangePacket<? extends State>> stateType, String stateName, Encryption encryption) {
        StateChangePacket<? extends State> stateChangePacket = stateType.newStateChangePacket();
        stateChangePacket.state = ((TcpServerHandler) ctx.handler()).getStateManager().getState(stateName);
        NetworkUtility.sendTcp(ctx, stateChangePacket, encryption);
    }
}
