/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.jmbnetty.server.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.jmb19905.jmbnetty.common.handler.Decoder;
import net.jmb19905.jmbnetty.server.ServerConnection;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import java.util.HashMap;
import java.util.Map;

public class TcpServerConnection extends ServerConnection {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final Map<TcpServerHandler, SocketChannel> clientConnections = new HashMap<>();

    public TcpServerConnection(int port) {
        super(port);
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void run() {
        TcpServerConnection instance = this;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            TcpServerHandler serverHandler = new TcpServerHandler(instance);
                            ch.pipeline().addLast(new Decoder(serverHandler.getEncryption()), serverHandler);
                            clientConnections.put(serverHandler, ch);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.bind(port).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            Logger.fatal(e);
            ShutdownManager.shutdown(-1);
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        super.stop();
    }

    public Map<TcpServerHandler, SocketChannel> getClientConnections() {
        return clientConnections;
    }
}
