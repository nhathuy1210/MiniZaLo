package server;

import java.net.*;
import java.io.*;
import java.util.*;
import client.model.Message;
import client.model.User;

// Lớp Server chính để quản lý kết nối với các client
public class Server {
    // Cổng mà server sẽ lắng nghe kết nối
    private static final int PORT = 9999;

    // Tập hợp các client đang kết nối (được đồng bộ hóa để tránh xung đột dữ liệu)
    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        // Khởi tạo ServerSocket để lắng nghe kết nối
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            
            // Vòng lặp vô hạn để chấp nhận các kết nối mới từ client
            while (true) {
                // Chấp nhận kết nối từ client
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Tạo một ClientHandler mới để xử lý client vừa kết nối
                ClientHandler clientHandler = new ClientHandler(socket, clients);

                // Thêm client vào danh sách các client đang kết nối
                clients.add(clientHandler);

                // Chạy ClientHandler trên một luồng riêng
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    // Phát sóng trạng thái online/offline của một user tới tất cả các client khác
    public static void broadcastOnlineStatus(int userId, boolean status) {
        User user = new User(userId, "", null);
        user.setOnline(status);
        
        synchronized(clients) {
            for (ClientHandler client : clients) {
                // Chỉ gửi trạng thái tới các client khác (trừ chính người dùng đó)
                if (client.getUser().getId() != userId) {
                    client.sendUserStatus(user);
                }
            }
        }
    }

    // Gửi danh sách người dùng đang online tới client mới kết nối
    private static void sendOnlineUsersTo(ClientHandler newClient) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                // Chỉ gửi trạng thái của những user khác đang online
                if (client != newClient && client.getUser() != null) {
                    User user = new User(client.getUser().getId(), "", null);
                    user.setOnline(true);
                    newClient.sendUserStatus(user);
                }
            }
        }
    }

    // Chuyển tiếp tin nhắn tới đúng người nhận dựa vào receiverId
    public static void forwardMessage(Message message) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                // Tìm client có ID trùng với người nhận và gửi tin nhắn
                if (client.getUser().getId() == message.getReceiverId()) {
                    client.sendMessage(message);
                    break; // Dừng lại sau khi đã gửi tin nhắn
                }
            }
        }
    }

    // Kiểm tra xem người dùng có đang online không
    public static boolean isUserOnline(int userId) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (client.getUser().getId() == userId) {
                    return true; // Trả về true nếu tìm thấy user online
                }
            }
        }
        return false; // Trả về false nếu không tìm thấy
    }

    // Lấy số lượng người dùng đang online
    public static int getOnlineCount() {
        return clients.size(); // Trả về kích thước của tập hợp client
    }

    // Lấy danh sách ID của tất cả người dùng đang online
    public static Set<Integer> getOnlineUserIds() {
        Set<Integer> userIds = new HashSet<>();
        synchronized(clients) {
            for (ClientHandler client : clients) {
                userIds.add(client.getUser().getId()); // Thêm ID vào tập hợp
            }
        }
        return userIds;
    }
}
