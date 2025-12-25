# Real-time Chat WebSocket - Image Support

## ✅ Đã thêm `isImage` field vào ChatMessage WebSocket

Chat real-time trong project collaboration bây giờ hỗ trợ cả **text** và **image**!

---

## 🔌 **WebSocket Configuration**

### **Endpoint**: 
```
ws://sketchnote.litecsys.com/ws
```

### **Subscribe to receive messages**:
```
/queue/private/{userId}
```

### **Send message**:
```
/app/chat.private
```

---

## 📋 **Message Format**

### **ChatMessage Structure**:
```json
{
  "senderId": 4,
  "senderName": "John Doe",
  "senderAvatarUrl": "https://...",
  "receiverId": 5,
  "content": "Hello world!",
  "isImage": false,
  "timestamp": "2025-12-25T12:00:00"
}
```

**Fields**:
- `senderId` (Long): ID người gửi
- `senderName` (String): Tên người gửi
- `senderAvatarUrl` (String): Avatar URL người gửi
- `receiverId` (Long): ID người nhận
- `content` (String): Nội dung
  - Nếu `isImage = false`: Text message
  - Nếu `isImage = true`: Image URL
- `isImage` (boolean): Flag phân biệt text/image
- `timestamp` (LocalDateTime): Thời gian gửi

---

## 🎯 **Frontend Integration**

### **1. Connect to WebSocket**

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// Connect
const socket = new SockJS('https://sketchnote.litecsys.com/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected:', frame);
    
    // Subscribe to receive messages
    const userId = 4; // Current user ID
    stompClient.subscribe('/queue/private/' + userId, function(message) {
        const chatMessage = JSON.parse(message.body);
        handleReceivedMessage(chatMessage);
    });
});
```

### **2. Send Text Message**

```javascript
function sendTextMessage(receiverId, text) {
    const message = {
        senderId: currentUserId,
        senderName: currentUserName,
        senderAvatarUrl: currentUserAvatar,
        receiverId: receiverId,
        content: text,
        isImage: false
    };
    
    stompClient.send('/app/chat.private', {}, JSON.stringify(message));
}
```

### **3. Send Image Message**

```javascript
async function sendImageMessage(receiverId, imageFile) {
    // 1. Upload image to S3
    const formData = new FormData();
    formData.append('file', imageFile);
    
    const uploadResponse = await fetch('/api/upload', {
        method: 'POST',
        body: formData
    });
    const { url: imageUrl } = await uploadResponse.json();
    
    // 2. Send WebSocket message with image URL
    const message = {
        senderId: currentUserId,
        senderName: currentUserName,
        senderAvatarUrl: currentUserAvatar,
        receiverId: receiverId,
        content: imageUrl,
        isImage: true
    };
    
    stompClient.send('/app/chat.private', {}, JSON.stringify(message));
}
```

### **4. Handle Received Message**

```javascript
function handleReceivedMessage(chatMessage) {
    if (chatMessage.isImage) {
        // Display image
        displayImageMessage(chatMessage);
    } else {
        // Display text
        displayTextMessage(chatMessage);
    }
}

function displayTextMessage(message) {
    const messageElement = `
        <div class="message">
            <img src="${message.senderAvatarUrl}" alt="${message.senderName}" />
            <div>
                <strong>${message.senderName}</strong>
                <p>${message.content}</p>
                <small>${formatTime(message.timestamp)}</small>
            </div>
        </div>
    `;
    // Append to chat container
}

function displayImageMessage(message) {
    const messageElement = `
        <div class="message">
            <img src="${message.senderAvatarUrl}" alt="${message.senderName}" />
            <div>
                <strong>${message.senderName}</strong>
                <img src="${message.content}" alt="Image message" class="chat-image" />
                <small>${formatTime(message.timestamp)}</small>
            </div>
        </div>
    `;
    // Append to chat container
}
```

---

## 🔄 **Backend Flow**

### **ChatWebSocketController**

```java
@MessageMapping("/chat.private")
public void sendMessage(@Payload ChatMessage chatMessage) {
    chatMessage.setTimestamp(LocalDateTime.now());
    
    // Send to receiver
    messagingTemplate.convertAndSend(
            "/queue/private/" + chatMessage.getReceiverId(), 
            chatMessage
    );
    
    // Send back to sender (confirmation)
    messagingTemplate.convertAndSend(
            "/queue/private/" + chatMessage.getSenderId(), 
            chatMessage
    );
}
```

**Flow**:
1. Frontend gửi message qua `/app/chat.private`
2. Backend nhận message
3. Set timestamp
4. Push đến receiver qua `/queue/private/{receiverId}`
5. Push lại sender qua `/queue/private/{senderId}` (confirmation)

---

## 🧪 **Testing**

### **Test với Postman/WebSocket Client**

1. **Connect** to `ws://sketchnote.litecsys.com/ws`
2. **Subscribe** to `/queue/private/4`
3. **Send** to `/app/chat.private`:

**Text message**:
```json
{
  "senderId": 5,
  "senderName": "Jane",
  "senderAvatarUrl": "https://...",
  "receiverId": 4,
  "content": "Hello!",
  "isImage": false
}
```

**Image message**:
```json
{
  "senderId": 5,
  "senderName": "Jane",
  "senderAvatarUrl": "https://...",
  "receiverId": 4,
  "content": "https://example.com/image.jpg",
  "isImage": true
}
```

4. **Receive** message on `/queue/private/4`

---

## 📝 **React Example**

```jsx
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function Chat({ currentUserId, receiverId }) {
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState(null);
    const [messageInput, setMessageInput] = useState('');

    useEffect(() => {
        // Connect WebSocket
        const socket = new SockJS('https://sketchnote.litecsys.com/ws');
        const client = Stomp.over(socket);
        
        client.connect({}, () => {
            // Subscribe to receive messages
            client.subscribe(`/queue/private/${currentUserId}`, (message) => {
                const chatMessage = JSON.parse(message.body);
                setMessages(prev => [...prev, chatMessage]);
            });
        });
        
        setStompClient(client);
        
        return () => client.disconnect();
    }, [currentUserId]);

    const sendMessage = (content, isImage = false) => {
        if (!stompClient) return;
        
        const message = {
            senderId: currentUserId,
            senderName: 'Current User',
            senderAvatarUrl: 'https://...',
            receiverId: receiverId,
            content: content,
            isImage: isImage
        };
        
        stompClient.send('/app/chat.private', {}, JSON.stringify(message));
        setMessageInput('');
    };

    const handleImageUpload = async (e) => {
        const file = e.target.files[0];
        // Upload to S3
        const formData = new FormData();
        formData.append('file', file);
        const res = await fetch('/api/upload', { method: 'POST', body: formData });
        const { url } = await res.json();
        
        // Send image message
        sendMessage(url, true);
    };

    return (
        <div>
            <div className="messages">
                {messages.map((msg, idx) => (
                    <div key={idx}>
                        {msg.isImage ? (
                            <img src={msg.content} alt="Image" />
                        ) : (
                            <p>{msg.content}</p>
                        )}
                    </div>
                ))}
            </div>
            
            <input 
                value={messageInput}
                onChange={(e) => setMessageInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && sendMessage(messageInput)}
            />
            <input type="file" accept="image/*" onChange={handleImageUpload} />
        </div>
    );
}
```

---

## ✅ **Summary**

- ✅ Thêm `isImage` field vào `ChatMessage`
- ✅ Frontend gửi message qua WebSocket với flag `isImage`
- ✅ Backend broadcast đến receiver và sender
- ✅ Frontend check `isImage` để render text hoặc image
- ✅ Hỗ trợ real-time cho cả text và image messages

Chat real-time bây giờ đã hoàn chỉnh! 🎉
