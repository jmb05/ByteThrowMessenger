package net.jmb19905.messenger.client;

public class CommandParser {

    public static void parseCommand(String fullCommand, MessagingClient client){
        String[] parts = fullCommand.split(" ");
        switch (parts[0]) {
            case "close":
                client.stop();
                break;
            case "connect": {
                String username = parts[1];
                client.connectWithOtherUser(username);
                break;
            }
            case "send": {
                String username = parts[1];
                client.sendToOtherUser(username, parts[2]);
                break;
            }
        }
    }

}
