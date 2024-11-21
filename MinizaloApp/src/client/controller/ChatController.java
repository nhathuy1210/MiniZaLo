package client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.collections.*;
import javafx.application.Platform;
import client.model.*;
import client.util.Notification;
import server.DatabaseConnection;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Optional;
import javafx.geometry.Insets;

public class ChatController {
    @FXML private ListView<User> userListView;
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Label selectedUserLabel;
    @FXML private Button sendButton;
    @FXML private MenuBar menuBar;
    @FXML private Menu settingsMenu;
    @FXML private Label userLabel;
    @FXML private ListView<User> searchResultsView;
    @FXML private TextField searchField;
    @FXML private MenuItem usernameMenuItem;
    @FXML private StackPane  rootContainer; 
    
    private User currentUser;
    private User selectedUser;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ObservableList<User> onlineUsers = FXCollections.observableArrayList();
    private volatile boolean isRunning = true;
    private Thread messageListener;
    private Notification notification;
    
    @FXML
    private void initialize() {
        userListView.setItems(onlineUsers);
        userListView.setCellFactory(lv -> new UserListCell());
        
        messageField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleSendMessage();
            }
        });
        
        userListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedUser = newVal;
                    selectedUserLabel.setText("Chatting with: " + newVal.getUsername());
                    newVal.setHasUnreadMessages(false);
                    userListView.refresh();
                    loadChatHistory();
                }
            }
        );

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch(newValue);
        });
    }

    
    private void handleSearch(String searchText) {
        if (searchText.isEmpty()) {
            userListView.setItems(onlineUsers);
            return;
        }
        
        ObservableList<User> searchResults = onlineUsers.filtered(user ->
            user.getUsername().toLowerCase().contains(searchText.toLowerCase())
        );
        userListView.setItems(searchResults);
    }

    @FXML
    private void handleLogout() {
        updateUserOnlineStatus(currentUser.getId(), false);
        navigateToLogin();
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Do you want to close Mini Zalo?");
        alert.setContentText("Choose your option.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateUserOnlineStatus(currentUser.getId(), false);
            Platform.exit();
        }
    }
    
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) searchField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not return to login window");
        }
    }
    
    private void updateUserOnlineStatus(int userId, boolean status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET online_status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setBoolean(1, status);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            
            // Thông báo cho server về trạng thái
            User statusUpdate = new User(userId, "", null);
            statusUpdate.setOnline(status);
            output.writeObject(statusUpdate);
            output.flush();
        } catch (SQLException | IOException e) {
            System.err.println("Could not update online status: " + e.getMessage());
        }
    }


    public void initData(User currentUser, Socket socket) {
        if (socket == null || socket.isClosed()) {
            throw new IllegalStateException("Valid socket connection required");
        }
        
        this.currentUser = currentUser;
        this.socket = socket;
        
        Platform.runLater(() -> {
            usernameMenuItem.setText("Username: " + currentUser.getUsername());
            notification = new Notification(rootContainer);
            notification.show("Welcome", "Connected as " + currentUser.getUsername());
        });
        
        try {
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
            output.writeObject(currentUser);
            output.flush();
            
            loadOnlineUsers();
            startMessageListener();
        } catch (IOException e) {
            showAlert("Connection Error", "Could not initialize chat connection");
            closeConnection();
        }
    }

    private void loadOnlineUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE id != ? AND online_status = true";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.getId());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    null
                );
                user.setOnline(true);
                Platform.runLater(() -> onlineUsers.add(user));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load online users");
        }
    }
    
    @FXML
    private void handleSendMessage() {
        String messageText = messageField.getText().trim();
        if (selectedUser == null || messageText.isEmpty()) {
            return;
        }

        Message message = new Message(currentUser.getId(), selectedUser.getId(), messageText);
        try {
            output.writeObject(message);
            output.flush();
            chatArea.appendText("Me: " + messageText + "\n");
            messageField.clear();
            saveMessageToDatabase(message);
        } catch (IOException e) {
            showAlert("Send Error", "Could not send message");
        }
    }

    private void saveMessageToDatabase(Message message) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, NOW())";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, message.getSenderId());
            pstmt.setInt(2, message.getReceiverId());
            pstmt.setString(3, message.getContent());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    private void loadChatHistory() {
        chatArea.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT m.*, u.username as sender_name FROM messages m " +
                          "JOIN users u ON m.sender_id = u.id " +
                          "WHERE (sender_id = ? AND receiver_id = ?) " +
                          "OR (sender_id = ? AND receiver_id = ?) " +
                          "ORDER BY timestamp";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentUser.getId());
            pstmt.setInt(2, selectedUser.getId());
            pstmt.setInt(3, selectedUser.getId());
            pstmt.setInt(4, currentUser.getId());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String sender = rs.getString("sender_name");
                String content = rs.getString("content");
                String timestamp = rs.getTimestamp("timestamp").toString();
                String prefix = (rs.getInt("sender_id") == currentUser.getId()) ? "Me" : sender;
                chatArea.appendText(String.format("[%s] %s: %s\n", timestamp, prefix, content));
            }
        } catch (SQLException e) {
            showAlert("History Error", "Could not load chat history");
        }
    }

    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Object received = input.readObject();
                    handleIncomingMessage(received);
                } catch (IOException | ClassNotFoundException e) {
                    if (isRunning && !socket.isClosed()) {
                        Platform.runLater(() -> showAlert("Connection Lost", "Lost connection to server"));
                        closeConnection();
                    }
                    break;
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleIncomingMessage(Object received) {
        if (received instanceof Message) {
            Message message = (Message) received;
            Platform.runLater(() -> {
                // Find sender's username from online users
                String senderName = onlineUsers.stream()
                    .filter(user -> user.getId() == message.getSenderId())
                    .map(User::getUsername)
                    .findFirst()
                    .orElse("Unknown User");
                
                // Show notification if not viewing current chat
                if (selectedUser == null || message.getSenderId() != selectedUser.getId()) {
                    showNotification(senderName, message.getContent());
                    
                    // Set unread message flag and update UI
                    onlineUsers.stream()
                        .filter(user -> user.getId() == message.getSenderId())
                        .findFirst()
                        .ifPresent(user -> {
                            user.setHasUnreadMessages(true);
                            userListView.refresh(); // This triggers the UserListCell to update
                        });
                }
                
                // Update chat area if sender is selected
                if (selectedUser != null && message.getSenderId() == selectedUser.getId()) {
                    chatArea.appendText(selectedUser.getUsername() + ": " + message.getContent() + "\n");
                    saveMessageToDatabase(message);
                }
            });
        } else if (received instanceof User) {
            User user = (User) received;
            Platform.runLater(() -> {
                if (user.isOnline()) {
                    if (!onlineUsers.contains(user)) {
                        onlineUsers.add(user);
                        showNotification("User Online", user.getUsername() + " is now online");
                    }
                } else {
                    onlineUsers.removeIf(u -> u.getId() == user.getId());
                    showNotification("User Offline", user.getUsername() + " is now offline");
                }
            });
        }
    }
    
    private void handleNewMessage(Message message) {
        Platform.runLater(() -> {
            userListView.getItems().forEach(user -> {
                if (user.getId() == message.getSenderId()) {
                    user.setHasUnreadMessages(true);
                    userListView.refresh();      
                }
            });
        });
    }

    

    private void showNotification(String title, String message) {
        Platform.runLater(() -> {
            if (notification == null && rootContainer != null) {
                notification = new Notification(rootContainer);
            }
            if (notification != null) {
                notification.show(title, message);
            }
        });
    }


    private void closeConnection() {
        isRunning = false;
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }	

    public void shutdown() {
        closeConnection();
    }
}

