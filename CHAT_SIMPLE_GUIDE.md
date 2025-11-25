# Chat Đơn Giản - Hướng Dẫn Sử Dụng

## 📋 Tổng Quan
Chat đơn giản giữa 2 người sử dụng:
- **REST API** (identity-service) - Lưu trữ messages vào database
- **WebSocket** (project-service) - Chat realtime giữa 2 người

---

## 🔌 WebSocket - Chat Realtime

### Kết Nối
```javascript
const socket = new SockJS('http://localhost:8082/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log('Connected');
});
```

### Subscribe Nhận Tin Nhắn
```javascript
// Subscribe để nhận tin nhắn
stompClient.subscribe('/queue/private/' + myUserId, (message) => {
  const chatMessage = JSON.parse(message.body);
  console.log('Received:', chatMessage);
  // chatMessage có: senderId, senderName, receiverId, content, timestamp
});
```

### Gửi Tin Nhắn
```javascript
// Gửi tin nhắn cho user khác
stompClient.send('/app/chat.private', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John',
  receiverId: 2,
  content: 'Hello!'
}));
```

---

## 🔄 REST API - Lưu Trữ Messages

### Gửi Message (Lưu vào DB)
```bash
POST /api/messages
Authorization: Bearer {token}
{
  "receiverId": 2,
  "content": "Hello!"
}
```

### Lấy Conversation (Lịch sử chat)
```bash
GET /api/messages/conversation/2?page=0&size=20
Authorization: Bearer {token}
```

### Lấy Danh Sách Conversations
```bash
GET /api/messages/conversations
Authorization: Bearer {token}
```

### Update Message
```bash
PUT /api/messages/{messageId}
Authorization: Bearer {token}
{
  "content": "Updated message"
}
```

### Delete Message
```bash
DELETE /api/messages/{messageId}
Authorization: Bearer {token}
```

---

## 📱 React Native Example

```javascript
import { useEffect, useState, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export function useChat(currentUserId, receiverId) {
  const [messages, setMessages] = useState([]);
  const stompClient = useRef(null);

  useEffect(() => {
    // Connect
    const socket = new SockJS('http://YOUR_SERVER:8082/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        // Subscribe to receive messages
        client.subscribe(`/queue/private/${currentUserId}`, (message) => {
          const msg = JSON.parse(message.body);
          setMessages(prev => [...prev, msg]);
        });
      }
    });
    
    client.activate();
    stompClient.current = client;

    return () => client.deactivate();
  }, [currentUserId]);

  const sendMessage = (content) => {
    if (stompClient.current) {
      stompClient.current.publish({
        destination: '/app/chat.private',
        body: JSON.stringify({
          senderId: currentUserId,
          receiverId: receiverId,
          content: content
        })
      });
    }
  };

  return { messages, sendMessage };
}
```

---

## 🎯 Cách Hoạt Động

1. **User A** gửi message qua WebSocket → Server nhận
2. Server gửi message đến:
   - `/queue/private/{User B ID}` - User B nhận realtime
   - `/queue/private/{User A ID}` - User A nhận confirmation
3. Đồng thời, có thể call REST API để lưu message vào DB

---

## 🧪 Test Nhanh

### Test với 2 Browser Windows:

**Window 1 (User 1):**
```javascript
const stompClient = Stomp.over(new SockJS('http://localhost:8082/ws'));
stompClient.connect({}, () => {
  stompClient.subscribe('/queue/private/1', (msg) => {
    console.log('User 1 received:', JSON.parse(msg.body));
  });
  
  // Gửi cho User 2
  stompClient.send('/app/chat.private', {}, JSON.stringify({
    senderId: 1,
    senderName: 'User 1',
    receiverId: 2,
    content: 'Hi User 2!'
  }));
});
```

**Window 2 (User 2):**
```javascript
const stompClient = Stomp.over(new SockJS('http://localhost:8082/ws'));
stompClient.connect({}, () => {
  stompClient.subscribe('/queue/private/2', (msg) => {
    console.log('User 2 received:', JSON.parse(msg.body));
  });
  
  // Gửi cho User 1
  stompClient.send('/app/chat.private', {}, JSON.stringify({
    senderId: 2,
    senderName: 'User 2',
    receiverId: 1,
    content: 'Hi User 1!'
  }));
});
```

---

## 📝 Message Format

```json
{
  "type": "CHAT",
  "senderId": 1,
  "senderName": "John Doe",
  "receiverId": 2,
  "content": "Hello!",
  "timestamp": "2025-11-25T01:30:00"
}
```

---

## ✅ Checklist

- [x] WebSocket chỉ có private chat (1-1)
- [x] REST API để lưu messages vào DB
- [x] Get conversation history
- [x] Update/Delete messages
- [x] Đơn giản, dễ sử dụng

---

## 🚀 Chạy Services

```bash
# Identity service (REST API)
cd identity-service
mvn spring-boot:run

# Project service (WebSocket)
cd project-service
mvn spring-boot:run
```

Xong! Giờ có thể chat giữa 2 người rồi! 🎉
