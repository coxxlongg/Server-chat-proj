package kz.ori;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final Server server;
    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // Authentication
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/login ")) {
                            String[] tokens = msg.split("\\s+");
                            String login = tokens[1];
                            String password = tokens[2];
                            if (server.getAuthenticationProvider().isAuthenticated(login, password)) {
                                if (server.isUserOnline(login)) {
                                    sendMessage("/login_failed User already logged in");
                                    logger.warning("Login failed for user " + login + ": User already logged in");
                                } else {
                                    sendMessage("/login_ok " + login);
                                    username = login;
                                    server.subscribe(this);
                                    logger.info("User logged in: " + username);
                                    break;
                                }
                            } else {
                                sendMessage("/login_failed Incorrect username or password");
                                logger.warning("Login failed for user " + login + ": Incorrect username or password");
                            }
                        }
                    }
                    // Communication loop
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/quit")) {
                            break;
                        }
                        if (msg.startsWith("/w ")) {
                            String[] tokens = msg.split("\\s+", 3);
                            String receiver = tokens[1];
                            String message = tokens[2];
                            server.sendPrivateMsg(this, receiver, message);
                        } else {
                            server.broadcastMessage(username + ": " + msg);
                        }
                        logger.info("Received message from " + username + ": " + msg);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error in client handler", e);
                } finally {
                    disconnect();
                }
            }).start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up client handler", e);
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            logger.info("Sent message to " + username + ": " + msg);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending message to " + username, e);
        }
    }

    public void disconnect() {
        try {
            server.unsubscribe(this);
            in.close();
            out.close();
            socket.close();
            logger.info("User disconnected: " + username);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error disconnecting user " + username, e);
        }
    }

    public String getUsername() {
        return username;
    }
}
