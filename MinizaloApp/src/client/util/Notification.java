package client.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

// Class Notification dùng để tạo và hiển thị thông báo trong ứng dụng
public class Notification {
    private final VBox notificationBox; // Hộp thông báo để chứa các thành phần (title, message)
    private final StackPane parentContainer; // StackPane cha để hiển thị thông báo

    // Constructor nhận vào StackPane làm container cha
    public Notification(StackPane parentContainer) {
        this.parentContainer = parentContainer;
        
        notificationBox = new VBox(5); // Tạo VBox với khoảng cách 5px giữa các phần tử
        notificationBox.setAlignment(Pos.TOP_LEFT); // Căn nội dung của VBox về góc trên bên trái
        notificationBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #e0e0e0;"); // Thiết lập style cho hộp thông báo
        notificationBox.setMaxWidth(250); // Đặt chiều rộng tối đa
        notificationBox.setMaxHeight(60); // Đặt chiều cao tối đa
        notificationBox.setOpacity(0); // Thiết lập độ mờ ban đầu là 0 (ẩn)

        // Tạo hiệu ứng đổ bóng cho hộp thông báo
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2)); // Màu đen với độ mờ 20%
        shadow.setRadius(4); // Đặt bán kính đổ bóng là 4px
        notificationBox.setEffect(shadow); // Áp dụng hiệu ứng đổ bóng cho notificationBox

        // Căn chỉnh notificationBox ở góc trên bên phải của StackPane
        StackPane.setAlignment(notificationBox, Pos.TOP_RIGHT);
        StackPane.setMargin(notificationBox, new Insets(5, 5, 0, 0)); // Thiết lập khoảng cách từ các cạnh
    }

    // Phương thức show dùng để hiển thị thông báo với tiêu đề và nội dung
    public void show(String title, String message) {
        // Tạo Label cho tiêu đề thông báo
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;"); // Thiết lập font cho tiêu đề
        
        // Tạo Label cho nội dung thông báo, nếu dài hơn 50 ký tự thì cắt ngắn lại
        Label messageLabel = new Label(message.length() > 50 ? message.substring(0, 47) + "..." : message);
        messageLabel.setStyle("-fx-font-size: 11;"); // Thiết lập font cho nội dung
        messageLabel.setWrapText(true); // Cho phép nội dung tự xuống dòng nếu quá dài
        
        notificationBox.getChildren().clear(); // Xóa các phần tử cũ trong notificationBox
        notificationBox.getChildren().addAll(titleLabel, messageLabel); // Thêm tiêu đề và nội dung mới
        
        // Kiểm tra nếu notificationBox chưa được thêm vào parentContainer thì thêm vào
        if (!parentContainer.getChildren().contains(notificationBox)) {
            parentContainer.getChildren().add(notificationBox);
        }

        // Tạo hiệu ứng fade-in khi thông báo xuất hiện
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notificationBox);
        fadeIn.setFromValue(0); // Độ mờ ban đầu là 0
        fadeIn.setToValue(0.9); // Độ mờ khi xuất hiện là 0.9 (gần như hoàn toàn)
        fadeIn.play(); // Bắt đầu hiệu ứng fade-in

        // Tạo khoảng dừng 3 giây trước khi thông báo biến mất
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            // Tạo hiệu ứng fade-out khi thông báo biến mất
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), notificationBox);
            fadeOut.setFromValue(0.9); // Độ mờ ban đầu là 0.9
            fadeOut.setToValue(0); // Độ mờ khi biến mất là 0
            fadeOut.setOnFinished(event -> parentContainer.getChildren().remove(notificationBox)); // Xóa notificationBox khỏi parentContainer sau khi ẩn
            fadeOut.play(); // Bắt đầu hiệu ứng fade-out
        });
        delay.play(); // Bắt đầu khoảng dừng
    }
}
