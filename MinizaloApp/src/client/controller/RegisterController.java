package client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import server.DatabaseConnection;
import java.sql.*;
import java.io.IOException;

// Lớp RegisterController điều khiển giao diện và logic của màn hình đăng ký
public class RegisterController {

    @FXML private TextField usernameField;           // Ô nhập tên đăng nhập
    @FXML private PasswordField passwordField;       // Ô nhập mật khẩu
    @FXML private PasswordField confirmPasswordField;// Ô nhập lại mật khẩu

    /**
     * Phương thức xử lý khi người dùng nhấn nút đăng ký.
     * Kiểm tra các điều kiện hợp lệ, sau đó lưu người dùng vào cơ sở dữ liệu.
     */
    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Kiểm tra nếu các trường bị bỏ trống
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Validation Error", "Vui lòng điền đầy đủ các trường");
            return;
        }

        // Kiểm tra nếu mật khẩu và xác nhận mật khẩu không khớp
        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Mật khẩu không khớp");
            return;
        }

        // Thực hiện lưu vào cơ sở dữ liệu
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Kiểm tra nếu tên người dùng đã tồn tại
            if (isUsernameTaken(conn, username)) {
                showAlert("Error", "Tên người dùng đã tồn tại, vui lòng chọn tên khác");
                return;
            }

            // Câu lệnh SQL để thêm người dùng mới
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            pstmt.executeUpdate();
            showAlert("Success", "Đăng ký thành công!");
            
            // Chuyển đến trang đăng nhập sau khi đăng ký thành công
            navigateToLogin();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Đăng ký thất bại, vui lòng thử lại");
        }
    }

    /**
     * Phương thức hiển thị hộp thoại cảnh báo.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(
            title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Kiểm tra xem tên người dùng đã tồn tại trong cơ sở dữ liệu hay chưa.
     */
    private boolean isUsernameTaken(Connection conn, String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }

    /**
     * Điều hướng đến màn hình đăng nhập sau khi đăng ký thành công.
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            
     
            scene.getStylesheets().add(getClass().getResource("/client/css/style.css").toExternalForm());
            
            stage.setTitle("Mini Zalo - Đăng nhập");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Không thể chuyển đến trang đăng nhập");
        }
    }

    /**
     * Xử lý khi người dùng nhấn nút quay lại trang đăng nhập.
     */
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/client/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Không thể quay lại trang đăng nhập");
        }
    }
}
