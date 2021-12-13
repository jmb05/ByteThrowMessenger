module net.jmb19905b.bytethrow.main {
    requires net.jmb19905b.utilities.main;
    requires net.jmb19905b.jmbnetty.main;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.talanlabs;
    requires org.mindrot;
    requires jdk.internal.opt;
    requires jdk.unsupported;
    requires io.netty.transport;
    requires com.formdev.flatlaf;
    requires io.netty.common;
    requires org.xerial.sqlitejdbc;
    exports net.jmb19905.bytethrow.common.packets;
    exports net.jmb19905.bytethrow.common.util;
    exports net.jmb19905.bytethrow.common;
    exports net.jmb19905.bytethrow.common.chat;
}