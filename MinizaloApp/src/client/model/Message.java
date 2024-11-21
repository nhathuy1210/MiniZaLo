package client.model;

import java.io.Serializable;
import java.time.LocalDateTime;


public class Message implements Serializable {
    
    // Các thuộc tính của Message
    private int id;                      // ID của tin nhắn (sử dụng nếu lưu vào database)
    private int senderId;                // ID của người gửi
    private int receiverId;              // ID của người nhận
    private String content;              // Nội dung của tin nhắn
    private LocalDateTime timestamp;     // Thời gian gửi tin nhắn

    // Constructor khởi tạo với senderId, receiverId và content
    public Message(int senderId, int receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now(); // Lấy thời gian hiện tại khi tạo tin nhắn
    }

    // Getter và Setter cho các thuộc tính

    // Lấy ID của tin nhắn
    public int getId() { 
        return id; 
    }

    // Đặt ID cho tin nhắn
    public void setId(int id) { 
        this.id = id; 
    }

    // Lấy ID của người gửi
    public int getSenderId() { 
        return senderId; 
    }

    // Đặt ID của người gửi
    public void setSenderId(int senderId) { 
        this.senderId = senderId; 
    }

    // Lấy ID của người nhận
    public int getReceiverId() { 
        return receiverId; 
    }

    // Đặt ID của người nhận
    public void setReceiverId(int receiverId) { 
        this.receiverId = receiverId; 
    }

    // Lấy nội dung tin nhắn
    public String getContent() { 
        return content; 
    }

    // Đặt nội dung tin nhắn
    public void setContent(String content) { 
        this.content = content; 
    }

    // Lấy thời gian gửi tin nhắn
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }

    // Đặt thời gian gửi tin nhắn (trong trường hợp cần chỉnh sửa)
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; 
    }
}
