package client.model;

import java.io.Serializable;

public class User implements Serializable {
    
    // Các thuộc tính của User
    private int id;                     // ID của người dùng
    private String username;            // Tên người dùng
    private String password;            // Mật khẩu (được mã hóa)
    private boolean online;             // Trạng thái online của người dùng
    private String lastSeen;            // Thời gian người dùng truy cập lần cuối
    private boolean hasUnreadMessages;  // Cờ đánh dấu nếu người dùng có tin nhắn chưa đọc
    
    // Constructor với các tham số id, username và password
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.online = false; // Mặc định là offline khi mới khởi tạo
    }

    // Constructor với chỉ username và password (sử dụng cho đăng nhập)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getter và Setter cho thuộc tính hasUnreadMessages
    // Kiểm tra xem người dùng có tin nhắn chưa đọc hay không
    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    // Đặt trạng thái tin nhắn chưa đọc cho người dùng
    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }

    // Các getter và setter cho các thuộc tính khác

    // Lấy ID của người dùng
    public int getId() { 
        return id; 
    }

    // Đặt ID cho người dùng
    public void setId(int id) { 
        this.id = id; 
    }

    // Lấy tên người dùng
    public String getUsername() { 
        return username; 
    }

    // Đặt tên người dùng
    public void setUsername(String username) { 
        this.username = username; 
    }

    // Lấy mật khẩu (chỉ sử dụng trong trường hợp cần thiết)
    public String getPassword() { 
        return password; 
    }

    // Đặt mật khẩu
    public void setPassword(String password) { 
        this.password = password; 
    }

    // Kiểm tra trạng thái online của người dùng
    public boolean isOnline() { 
        return online; 
    }

    // Đặt trạng thái online/offline cho người dùng
    public void setOnline(boolean online) { 
        this.online = online; 
    }

    // Lấy thời gian truy cập cuối cùng của người dùng
    public String getLastSeen() { 
        return lastSeen; 
    }

    // Đặt thời gian truy cập cuối cùng
    public void setLastSeen(String lastSeen) { 
        this.lastSeen = lastSeen; 
    }
}
