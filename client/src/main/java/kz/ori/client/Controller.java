package kz.ori.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller implements Initializable {

    @FXML
    private TextField msgField, loginField, newNickField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private TextArea msgArea;

    @FXML
    private HBox loginBox, msgBox, changeNickBox;

    @FXML
    ListView<String> clientsList;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private static final String HISTORY_FILE_PATH = "chat_history.txt";

    public void setUsername(String username) {
        this.username = username;
        if (this.username == null) {
            loginBox.setVisible(true);
            loginBox.setManaged(true);
            msgBox.setVisible(false);
            msgBox.setManaged(false);
            changeNickBox.setVisible(false);
            changeNickBox.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        } else {
            loginBox.setVisible(false);
            loginBox.setManaged(false);
            msgBox.setVisible(true);
            msgBox.setManaged(true);
            changeNickBox.setVisible(true);
            changeNickBox.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    // цикл авторизации
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/login_ok ")) {
                            setUsername(msg.split("\\s+")[1]);
                            break;
                        }
                        if (msg.startsWith("/login_failed ")) {
                            String reason = msg.split("\\s+", 2)[1];
                            msgArea.appendText(reason + "\n");
                            writeToHistoryFile(reason);
                        }
                    }
                    // цикл общения
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/clients_list ")) {
                            Platform.runLater(() -> {
                                clientsList.getItems().clear();
                                List<String> clients = Stream.of(msg.split("\\s+"))
                                        .skip(1)
                                        .collect(Collectors.toList());
                                clientsList.getItems().addAll(clients);
                            });
                            continue;
                        }
                        if (msg.startsWith("/nick_changed ")) {
                            String[] tokens = msg.split("\\s+");
                            if (tokens[1].equals(username)) {
                                setUsername(tokens[2]);
                                msgArea.appendText("Your nickname has been changed to " + tokens[2] + "\n");
                                writeToHistoryFile("Your nickname has been changed to " + tokens[2]);
                            } else {
                                msgArea.appendText(tokens[1] + " changed nickname to " + tokens[2] + "\n");
                                writeToHistoryFile(tokens[1] + " changed nickname to " + tokens[2]);
                            }
                            continue;
                        }
                        msgArea.appendText(msg + "\n");
                        writeToHistoryFile(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to connect to server");
        }
    }

    public void login() {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        if (loginField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            out.writeUTF("/login " + loginField.getText() + " " + passwordField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        setUsername(null);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение");
            alert.showAndWait();
        }
    }

    public void changeNick() {
        if (newNickField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Новое имя пользователя не может быть пустым", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            out.writeUTF("/change_nick " + newNickField.getText() + " " + username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUsername(null);
    }

    private void writeToHistoryFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write("[" + timestamp + "] " + message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
