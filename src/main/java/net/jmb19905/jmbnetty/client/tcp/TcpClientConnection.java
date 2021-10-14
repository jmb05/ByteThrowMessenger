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

package net.jmb19905.jmbnetty.client.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.jmbnetty.client.ClientConnection;
import net.jmb19905.jmbnetty.common.handler.Decoder;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import java.net.ConnectException;

public class TcpClientConnection extends ClientConnection {

    private final EventLoopGroup group;
    private TcpClientHandler clientHandler;
    private SocketChannel channel;

    public TcpClientConnection(int port, String remoteAddress) {
        super(port, remoteAddress);
        this.group = new NioEventLoopGroup();
    }

    @Override
    public void run() {
        clientHandler = new TcpClientHandler(this);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new Decoder(clientHandler.getEncryption()), clientHandler);
                            channel = ch;
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture syncFuture = b.connect(getRemoteAddress(), getPort()).sync();
            Channel channel = syncFuture.channel();
            ChannelFuture closeFuture = channel.closeFuture();
            closeFuture.await();
        } catch (InterruptedException e) {
            Logger.fatal(e, "Client closed in non-standard way -> crashing");
            ShutdownManager.shutdown(-1);
        } catch (Exception e){
            Logger.error(e);
            StartClient.guiManager.showLocalisedError(Localisation.get("no_internet"));
            ShutdownManager.shutdown(-1);
        }
    }

    public TcpClientHandler getClientHandler() {
        return clientHandler;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void stop() {
        group.shutdownGracefully();
        super.stop();
    }
}
