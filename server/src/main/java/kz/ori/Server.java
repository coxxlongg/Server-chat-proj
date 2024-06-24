package kz.ori;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private int port;
    private List<ClientHandler> list;
    private AuthenticationProvider authenticationProvider;

    public Server(int port) {
        this.port = port;
        this.list = new ArrayList<>();
        this.authenticationProvider = new InMemoryAuthProvider();

        // Load logging configuration
        try (FileInputStream configFile = new FileInputStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (IOException e) {
            System.err.println("Could not setup logger configuration: " + e.toString());
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port " + port);
            System.out.println("Server started on port " + port + ". Waiting for client connection...");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String msg) {
        for (ClientHandler clientHandler : list) {
            clientHandler.sendMessage(msg);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        list.add(clientHandler);
        sendClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        list.remove(clientHandler);
        sendClientList();
    }

    public boolean isUserOnline(String username) {
        for (ClientHandler clientHandler : list) {
            if (clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void sendPrivateMsg(ClientHandler sender, String receiver, String msg) {
        for (ClientHandler c : list) {
            if (c.getUsername().equals(receiver)) {
                c.sendMessage("From: " + sender.getUsername() + " Message: " + msg);
                sender.sendMessage("Receiver: " + receiver + " Message: " + msg);
                return;
            }
        }
        sender.sendMessage("Unable to send message to " + receiver);
    }

    public void sendClientList() {
        StringBuilder builder = new StringBuilder("/clients_list ");
        for (ClientHandler c : list) {
            builder.append(c.getUsername()).append(" ");
        }
        builder.setLength(builder.length() - 1);
        // /clients_list Bob Alex John
        String clientList = builder.toString();
        for (ClientHandler c : list) {
            c.sendMessage(clientList);
        }
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    // Getter for ClientHandlers list (for testing purposes)
    public List<ClientHandler> getClientHandlers() {
        return list;
    }
}
