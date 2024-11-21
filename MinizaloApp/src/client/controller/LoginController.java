package client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import server.DatabaseConnection;
import client.model.User;

import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    private Socket socket;

    // Xử lý sự kiện đăng nhập
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Kiểm tra thông tin đăng nhập
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Vui lòng nhập tên đăng nhập và mật khẩu");
            return;
        }

        try {
            // Thiết lập kết nối socket với máy chủ chat
            socket = new Socket("localhost", 9999);

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Không thể kết nối với cơ sở dữ liệu");
                    return;
                }

                // Truy vấn để kiểm tra tài khoản
                String query = "SELECT * FROM users WHERE username = ? AND password = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            User user = new User(rs.getInt("id"), username, password);
                            updateUserOnlineStatus(user.getId(), true);
                            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Chào mừng, " + username + "!");
                            openChatWindow(user);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Login Failed", "Tên đăng nhập hoặc mật khẩu không đúng");
                        }
                    }
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Không thể kết nối với máy chủ chat");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Lỗi khi truy vấn cơ sở dữ liệu");
        }
    }

    // Cập nhật trạng thái online của người dùng
    private void updateUserOnlineStatus(int userId, boolean status) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                String query = "UPDATE users SET online_status = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setBoolean(1, status);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Không thể cập nhật trạng thái online: " + e.getMessage());
        }
    }

    // Mở cửa sổ chat
    private void openChatWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/chat.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.initData(user, socket);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client/css/style.css").toExternalForm());

            stage.setTitle("Mini Zalo - Chat");
            stage.setScene(scene);
            stage.setMaximized(true);

            // Xử lý khi đóng cửa sổ chat
            stage.setOnCloseRequest(event -> {
                updateUserOnlineStatus(user.getId(), false);
                closeSocket();
            });

            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Không thể mở cửa sổ chat");
        }
    }

    // Đóng kết nối socket
    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đóng socket: " + e.getMessage());
        }
    }

    // Chuyển sang màn hình đăng ký
    @FXML
    private void handleRegisterNavigation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Không thể mở trang đăng ký");
        }
    }

    // Hiển thị thông báo
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
