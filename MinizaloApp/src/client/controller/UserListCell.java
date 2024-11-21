package client.controller;

import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import client.model.User;

// Lớp UserListCell kế thừa ListCell<User>, dùng để tạo cell tùy chỉnh cho danh sách người dùng trong ListView.
public class UserListCell extends ListCell<User> {

    // Các thành phần giao diện cho từng cell
    private HBox content;          // HBox chứa các thành phần của cell
    private Circle statusDot;      // Chấm tròn hiển thị trạng thái online/offline
    private Label nameLabel;       // Nhãn hiển thị tên người dùng
    private Circle unreadIndicator;// Chấm tròn hiển thị tin nhắn chưa đọc

    // Constructor khởi tạo cell
    public UserListCell() {
        super();

        // Khởi tạo chấm tròn trạng thái (online/offline)
        statusDot = new Circle(5);

        // Khởi tạo nhãn tên người dùng
        nameLabel = new Label();

        // Khởi tạo chấm tròn thông báo tin nhắn chưa đọc (mặc định ẩn)
        unreadIndicator = new Circle(4, Color.RED);
        unreadIndicator.setVisible(false);
        
        // Thiết lập HBox chứa các thành phần trên và căn lề trái
        content = new HBox(10);
        content.getChildren().addAll(statusDot, nameLabel, unreadIndicator);
        content.setAlignment(Pos.CENTER_LEFT);
    }

    // Phương thức cập nhật nội dung của cell khi có dữ liệu thay đổi
    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);

        // Nếu cell trống hoặc không có dữ liệu người dùng thì xóa nội dung hiển thị
        if (empty || user == null) {
            setGraphic(null);
        } else {
            // Thiết lập tên người dùng vào nhãn
            nameLabel.setText(user.getUsername());

            // Kiểm tra nếu người dùng có tin nhắn chưa đọc thì làm đậm tên người dùng
            if (user.hasUnreadMessages()) {
                nameLabel.setStyle("-fx-font-weight: bold;");
                unreadIndicator.setVisible(true); // Hiển thị chấm tròn thông báo
            } else {
                nameLabel.setStyle("");
                unreadIndicator.setVisible(false); // Ẩn chấm tròn thông báo
            }

            // Thiết lập màu sắc của chấm tròn trạng thái (Xanh lá nếu online, Xám nếu offline)
            statusDot.setFill(user.isOnline() ? Color.GREEN : Color.GRAY);

            // Hiển thị nội dung của cell
            setGraphic(content);
        }
    }
}
