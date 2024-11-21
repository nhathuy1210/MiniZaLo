package server;

import java.net.*;
import java.io.*;
import java.util.*;
import client.model.*;

// Lớp xử lý các kết nối client và thực thi trên một luồng riêng
public class ClientHandler implements Runnable {
    private final Socket socket; // Socket kết nối với client
    private ObjectInputStream input; // Luồng nhận dữ liệu từ client
    private ObjectOutputStream output; // Luồng gửi dữ liệu tới client
    private final Set<ClientHandler> clients; // Danh sách các client đang kết nối
    private User user; // Thông tin người dùng
    private volatile boolean running = true; // Cờ kiểm tra trạng thái hoạt động

    // Constructor khởi tạo với socket và danh sách client
    public ClientHandler(Socket socket, Set<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
        initializeStreams(); // Khởi tạo các luồng vào/ra
    }

    // Phương thức khởi tạo các luồng
    private void initializeStreams() {
        try {
            // Tạo luồng gửi dữ liệu
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            // Tạo luồng nhận dữ liệu
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Stream initialization error: " + e.getMessage());
            closeEverything(); // Đóng kết nối nếu có lỗi
        }
    }

    // Trả về thông tin người dùng
    public User getUser() {
        return user;
    }

    // Gửi tin nhắn tới client
    public void sendMessage(Message message) {
        try {
            output.writeObject(message); // Gửi đối tượng tin nhắn
            output.flush();
        } catch (IOException e) {
            System.out.println("Message send error: " + e.getMessage());
        }
    }

    // Gửi trạng thái online/offline của user tới client khác
    public void sendUserStatus(User statusUser) {
        try {
            output.writeObject(statusUser); // Gửi đối tượng trạng thái user
            output.flush();
        } catch (IOException e) {
            System.out.println("Status broadcast error: " + e.getMessage());
        }
    }

    // Phương thức chính được thực thi khi luồng bắt đầu
    @Override
    public void run() {
        if (!isActive()) return; // Kiểm tra nếu kết nối không hoạt động
        
        try {
            // Đọc thông tin người dùng từ client
            Object userObj = input.readObject();
            if (userObj instanceof User) {
                this.user = (User) userObj;
                broadcastUserStatus(true); // Thông báo user đã online
                sendOnlineUsersTo(this); // Gửi danh sách user đang online cho client mới
            }

            // Vòng lặp lắng nghe tin nhắn từ client
            while (running && isActive()) {
                Object messageObj = input.readObject(); // Nhận tin nhắn từ client
                handleMessage(messageObj); // Xử lý tin nhắn
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            closeEverything(); // Đóng kết nối khi hoàn tất
        }
    }

    // Xử lý tin nhắn từ client
    private void handleMessage(Object messageObj) {
        if (messageObj instanceof Message) {
            Message message = (Message) messageObj;
            // Duyệt qua danh sách client để gửi tin nhắn cho đúng người nhận
            synchronized(clients) {
                for (ClientHandler client : clients) {
                    if (client.user != null && client.user.getId() == message.getReceiverId()) {
                        client.sendMessage(message); // Gửi tin nhắn cho người nhận
                        break;
                    }
                }
            }
        }
    }

    // Phát broadcast trạng thái của user (online/offline)
    private void broadcastUserStatus(boolean online) {
        if (user != null) {
            user.setOnline(online); // Cập nhật trạng thái online của user
            synchronized(clients) {
                for (ClientHandler client : clients) {
                    // Gửi trạng thái tới tất cả các client khác
                    if (client != this && client.isActive()) {
                        client.sendUserStatus(user);
                    }
                }
            }
        }
    }

    // Gửi danh sách người dùng đang online cho client mới
    private void sendOnlineUsersTo(ClientHandler newClient) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client != newClient && client.user != null) {
                    // Tạo một đối tượng user online để gửi
                    User onlineUser = new User(client.user.getId(), client.user.getUsername(), null);
                    onlineUser.setOnline(true);
                    newClient.sendUserStatus(onlineUser); // Gửi thông tin user đang online
                }
            }
        }
    }

    // Kiểm tra trạng thái hoạt động của client
    private boolean isActive() {
        return running && socket != null && !socket.isClosed() && output != null && input != null;
    }

    // Đóng tất cả kết nối và luồng khi kết thúc
    private void closeEverything() {
        running = false; // Đặt cờ chạy về false
        broadcastUserStatus(false); // Thông báo user đã offline
        
        synchronized(clients) {
            clients.remove(this); // Xóa client khỏi danh sách
        }
        
        try {
            if (input != null) input.close(); // Đóng luồng nhận
            if (output != null) output.close(); // Đóng luồng gửi
            if (socket != null && !socket.isClosed()) socket.close(); // Đóng socket
        } catch (IOException e) {
            System.out.println("Resource closure error: " + e.getMessage());
        }
    }
}
