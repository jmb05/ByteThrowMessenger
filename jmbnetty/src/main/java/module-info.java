module net.jmb19905b.jmbnetty.main {
    requires net.jmb19905b.utilities.main;
    requires io.netty.transport;
    requires org.jetbrains.annotations;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.handler;
    requires com.google.common;
    exports net.jmb19905.jmbnetty.client;
    exports net.jmb19905.jmbnetty.server;
    exports net.jmb19905.jmbnetty.common.crypto;
    exports net.jmb19905.jmbnetty.common.packets.registry;
    exports net.jmb19905.jmbnetty.client.tcp;
    exports net.jmb19905.jmbnetty.common.handler;
    exports net.jmb19905.jmbnetty.utility;
    exports net.jmb19905.jmbnetty.common.exception;
    exports net.jmb19905.jmbnetty.common.packets.handler;
    exports net.jmb19905.jmbnetty.server.tcp;
    exports net.jmb19905.jmbnetty.common.connection.event;
    exports net.jmb19905.jmbnetty.common.connection;
    exports net.jmb19905.jmbnetty.common.state;
    exports net.jmb19905.jmbnetty.common.handler.event;
}