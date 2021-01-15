package net.jmb19905.test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        EMLogger.init();
        EMLogger.setLevel(EMLogger.LEVEL_DEBUG);

        Node serverClient1Node = new Node();
        Node serverClient2Node = new Node();

        Node client1ServerNode = new Node();
        Node client1Client2Node = new Node();

        Node client2ServerNode = new Node();
        Node client2Client1Node = new Node();

        client1ServerNode.setReceiverPublicKey(serverClient1Node.getPublicKey());
        serverClient1Node.setReceiverPublicKey(client1ServerNode.getPublicKey());

        client2ServerNode.setReceiverPublicKey(serverClient2Node.getPublicKey());
        serverClient2Node.setReceiverPublicKey(client2ServerNode.getPublicKey());

        String s = "HALLO";
        String encryptedS = "";

        Server server = new Server();
        Kryo serverKryo = server.getKryo();
        serverKryo.register(TestMessage.class);



        List<Connection> connections = new ArrayList<>();

        server.addListener(new Listener(){

            @Override
            public void connected(Connection connection) {
                connections.add(connection);
                System.out.println("Connection: " + connection.getRemoteAddressTCP());
            }

            @Override
            public void received(Connection connection, Object o) {
                TestMessage message = (TestMessage) o;
                byte[] decryptedKey = serverClient1Node.decrypt(message.key);
                message.key = serverClient2Node.encrypt(decryptedKey);
                connections.get(1).sendTCP(message);
            }
        });

        server.start();
        try {
            server.bind(10000, 10001);
        } catch (IOException e) {
            e.printStackTrace();
        }



        Client client1 = new Client();
        Kryo client1Kryo = client1.getKryo();
        client1Kryo.register(TestMessage.class);



        client1.start();
        try {
            client1.connect(5000, "localhost", 10000, 10001);
        } catch (IOException e) {
            e.printStackTrace();
        }



        Client client2 = new Client();
        Kryo client2Kryo = client2.getKryo();
        client2Kryo.register(TestMessage.class);

        client2.addListener(new Listener(){
            @Override
            public void received(Connection connection, Object o) {
                TestMessage message = (TestMessage) o;
                byte[] key = client1ServerNode.decrypt(message.key);
                PublicKey publicKey = Util.createPublicKeyFromData(key);
                System.out.println(publicKey);
            }
        });

        client2.start();
        try {
            client2.connect(5000, "localhost", 10000, 10001);
        } catch (IOException e) {
            e.printStackTrace();
        }



        //encryptedS = client1Client2Node.encrypt(s);
        TestMessage message = new TestMessage();
        message.key = client1Client2Node.getPublicKey().getEncoded();
        System.out.println(client1Client2Node.getPublicKey());
        client1.sendTCP(message);

    }


    private static class TestMessage{

        public byte[] key;

    }

}
