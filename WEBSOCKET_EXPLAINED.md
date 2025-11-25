# WebSocket Chat - Giải Thích Chi Tiết

## ❓ Làm Sao Biết Ai Sẽ Subscribe & Nhận Message?

### 🔑 Nguyên Lý Cơ Bản

**Mỗi user có 1 "hộp thư" riêng** được xác định bởi userId:
- User 1 → `/queue/private/1`
- User 2 → `/queue/private/2`
- User 3 → `/queue/private/3`

### 📝 Quy Trình Hoạt Động

#### Bước 1: User Connect & Subscribe
```javascript
// User 1 kết nối và "mở hộp thư" của mình
const stompClient = Stomp.over(new SockJS('http://localhost:8082/ws'));

stompClient.connect({}, () => {
  // Subscribe = "Mở hộp thư" để nhận tin
  stompClient.subscribe('/queue/private/1', (message) => {
    const msg = JSON.parse(message.body);
    console.log('Nhận được:', msg.content);
  });
});
```

#### Bước 2: Gửi Message
```javascript
// User 1 gửi tin cho User 2
stompClient.send('/app/chat.private', {}, JSON.stringify({
  senderId: 1,        // Tôi là User 1
  receiverId: 2,      // Gửi cho User 2
  content: 'Hello!'
}));
```

#### Bước 3: Server Xử Lý
```java
@MessageMapping("/chat.private")
public void sendMessage(@Payload ChatMessage chatMessage) {
    // Server nhận message từ User 1
    // Biết gửi cho User 2 vì có receiverId = 2
    
    // Gửi vào "hộp thư" của User 2
    messagingTemplate.convertAndSend(
        "/queue/private/" + chatMessage.getReceiverId(),  // /queue/private/2
        chatMessage
    );
}
```

#### Bước 4: User 2 Nhận Message
```javascript
// User 2 đã subscribe vào /queue/private/2
// Nên sẽ nhận được message tự động!
stompClient.subscribe('/queue/private/2', (message) => {
  const msg = JSON.parse(message.body);
  console.log('User 2 nhận:', msg.content); // "Hello!"
});
```

---

## 🎯 Ví Dụ Thực Tế

### Scenario: User A chat với User B

```javascript
// ============ USER A (ID: 1) ============
const userA = Stomp.over(new SockJS('http://localhost:8082/ws'));

userA.connect({}, () => {
  console.log('User A connected');
  
  // User A subscribe vào hộp thư của mình
  userA.subscribe('/queue/private/1', (msg) => {
    const message = JSON.parse(msg.body);
    console.log('User A nhận:', message);
  });
  
  // User A gửi tin cho User B
  setTimeout(() => {
    userA.send('/app/chat.private', {}, JSON.stringify({
      senderId: 1,
      senderName: 'User A',
      receiverId: 2,
      content: 'Chào User B!'
    }));
  }, 1000);
});

// ============ USER B (ID: 2) ============
const userB = Stomp.over(new SockJS('http://localhost:8082/ws'));

userB.connect({}, () => {
  console.log('User B connected');
  
  // User B subscribe vào hộp thư của mình
  userB.subscribe('/queue/private/2', (msg) => {
    const message = JSON.parse(msg.body);
    console.log('User B nhận:', message);
    // → Sẽ log: {senderId: 1, content: 'Chào User B!', ...}
    
    // User B reply
    userB.send('/app/chat.private', {}, JSON.stringify({
      senderId: 2,
      senderName: 'User B',
      receiverId: 1,
      content: 'Chào User A!'
    }));
  });
});
```

---

## 📊 Sơ Đồ Luồng Dữ Liệu

```
┌─────────────┐                                    ┌─────────────┐
│   User 1    │                                    │   User 2    │
│  (ID: 1)    │                                    │  (ID: 2)    │
└──────┬──────┘                                    └──────┬──────┘
       │                                                  │
       │ 1. Connect                                       │ 1. Connect
       │ 2. Subscribe('/queue/private/1')                 │ 2. Subscribe('/queue/private/2')
       │                                                  │
       ▼                                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                         WebSocket Server                         │
│                                                                  │
│  Queue Registry:                                                │
│  • /queue/private/1 → User 1 đang subscribe                     │
│  • /queue/private/2 → User 2 đang subscribe                     │
└─────────────────────────────────────────────────────────────────┘
       │                                                  │
       │ 3. Send message                                  │
       │    {senderId: 1, receiverId: 2}                  │
       │──────────────────────────────────────────────────▶
       │                                                  │
       │                                                  │ 4. Receive message
       │                                                  │    at /queue/private/2
       │                                                  │
       │ 5. Confirmation                                  │
       │    (sent back to /queue/private/1)               │
       │◀─────────────────────────────────────────────────│
```

---

## 🔐 Bảo Mật & Logic

### Câu Hỏi: Nếu User 3 subscribe vào `/queue/private/1` thì sao?

**Trả lời:** Được! Nhưng không nên:
- WebSocket mặc định không có authentication cho từng subscription
- Nên thêm security check ở server side
- Hoặc dùng JWT token trong WebSocket handshake

### Cách Bảo Mật Tốt Hơn:

```java
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        
        // Chỉ cho phép user subscribe vào queue của chính họ
        config.enableSimpleBroker("/queue", "/topic")
              .setUserDestinationPrefix("/user");
    }
}
```

Sau đó dùng:
```java
// Gửi đến user cụ thể (Spring tự động check)
messagingTemplate.convertAndSendToUser(
    username,  // Spring sẽ check user này có quyền không
    "/queue/private",
    message
);
```

---

## 💡 Tóm Tắt

| Khái Niệm | Giải Thích | Ví Dụ |
|-----------|------------|-------|
| **Subscribe** | "Mở hộp thư" để nhận tin | `subscribe('/queue/private/1')` |
| **Queue** | "Hộp thư" của mỗi user | `/queue/private/{userId}` |
| **Send** | Gửi tin nhắn | `send('/app/chat.private', message)` |
| **receiverId** | Chỉ định người nhận | `{receiverId: 2}` |
| **Server** | Chuyển tin đến đúng queue | `convertAndSend('/queue/private/2')` |

---

## ✅ Checklist Để Chat Hoạt Động

- [ ] User 1 connect WebSocket
- [ ] User 1 subscribe `/queue/private/1`
- [ ] User 2 connect WebSocket  
- [ ] User 2 subscribe `/queue/private/2`
- [ ] User 1 gửi message với `receiverId: 2`
- [ ] Server nhận và forward đến `/queue/private/2`
- [ ] User 2 nhận message vì đã subscribe!

---

## 🎓 Hiểu Đơn Giản

**Giống như hệ thống bưu điện:**
1. Mỗi người có 1 hộp thư (queue) với số nhà (userId)
2. Bạn phải "mở hộp thư" (subscribe) để nhận thư
3. Khi gửi thư, bạn ghi địa chỉ người nhận (receiverId)
4. Bưu điện (server) chuyển thư đến đúng hộp thư
5. Người nhận mở hộp thư và đọc thư!

**Không mở hộp thư (không subscribe) = Không nhận được thư!**
