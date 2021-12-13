package net.jmb19905.demo;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.demo.managers.ClientManager;
import net.jmb19905.demo.managers.EndpointManager;
import net.jmb19905.demo.managers.ServerManager;
import net.jmb19905.demo.util.RegistryManager;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import javax.swing.*;
import java.net.ConnectException;

public class Demo {

    public static EndpointManager manager;

    public static void main(String[] args) {
        try {
            ShutdownManager.addCleanUp(Logger::close);
            Logger.initLogFile("demo");

            RegistryManager.registerPackets();
            Logger.info("Registered Packets");

            FlatDarculaLaf.setup(); // Init L&F

            StartOption startOption = getStartOption();

            switch (startOption) {
                case SERVER -> manager = new ServerManager();
                case CLIENT -> {
                    String serverAddress = JOptionPane.showInputDialog("Server address (default: 'localhost'):");
                    if (serverAddress.isBlank()) serverAddress = "localhost";
                    manager = new ClientManager(serverAddress);
                }
                case CANCEL -> ShutdownManager.shutdown(0);
            }
        }catch (Exception e) {
            Logger.error(e);
            if(e instanceof ConnectException) JOptionPane.showMessageDialog(null, "Connection refused", "", JOptionPane.ERROR_MESSAGE);
        }
    }

    private enum StartOption{
        SERVER,CLIENT,CANCEL
    }

    private static StartOption getStartOption() {
        String[] options = {"Server", "Client"};

        int startOptionAsInt = JOptionPane.showOptionDialog(null, "Start as...", "",
                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon(),
                                options, options[0]);
        return startOptionAsInt == -1 ? StartOption.CANCEL : (startOptionAsInt == 0 ? StartOption.SERVER : StartOption.CLIENT);
    }

}