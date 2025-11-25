# Chat API Documentation

## Overview
This document describes the Chat API implementation for SketchNote, including both REST API endpoints (identity-service) for message persistence and WebSocket endpoints (project-service) for real-time chat.

---

## 1. REST API - Message CRUD (identity-service)

### Base URL
```
http://localhost:8081/api/messages
```

### Endpoints

#### 1.1 Send Message
**POST** `/api/messages`

Send a new message to another user.

**Request Body:**
```json
{
  "receiverId": 2,
  "content": "Hello! How are you?"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Message sent successfully",
  "result": {
    "id": 1,
    "senderId": 1,
    "senderName": "John Doe",
    "senderAvatarUrl": "https://example.com/avatar1.jpg",
    "receiverId": 2,
    "receiverName": "Jane Smith",
    "receiverAvatarUrl": "https://example.com/avatar2.jpg",
    "content": "Hello! How are you?",
    "createdAt": "2025-11-25T01:30:00",
    "updatedAt": "2025-11-25T01:30:00",
    "isDeleted": false
  }
}
```

---

#### 1.2 Get Conversation
**GET** `/api/messages/conversation/{userId}?page=0&size=20`

Get paginated conversation between current user and another user.

**Path Parameters:**
- `userId`: ID of the other user

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)

**Response:**
```json
{
  "code": 200,
  "message": "Conversation retrieved successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "senderId": 1,
        "senderName": "John Doe",
        "receiverId": 2,
        "receiverName": "Jane Smith",
        "content": "Hello!",
        "createdAt": "2025-11-25T01:30:00",
        "isDeleted": false
      }
    ],
    "pageable": {...},
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "size": 20,
    "number": 0
  }
}
```

---

#### 1.3 Get All Conversations
**GET** `/api/messages/conversations`

Get all conversations for the current user with last message info.

**Response:**
```json
{
  "code": 200,
  "message": "Conversations retrieved successfully",
  "result": [
    {
      "userId": 2,
      "userName": "Jane Smith",
      "userAvatarUrl": "https://example.com/avatar2.jpg",
      "lastMessage": "See you tomorrow!",
      "lastMessageTime": "2025-11-25T01:30:00",
      "unreadCount": 3
    }
  ]
}
```

---

#### 1.4 Get Message by ID
**GET** `/api/messages/{messageId}`

Get a specific message by ID.

**Response:**
```json
{
  "code": 200,
  "message": "Message retrieved successfully",
  "result": {
    "id": 1,
    "senderId": 1,
    "senderName": "John Doe",
    "content": "Hello!",
    ...
  }
}
```

---

#### 1.5 Update Message
**PUT** `/api/messages/{messageId}`

Update a message (only sender can update).

**Request Body:**
```json
{
  "content": "Hello! How are you doing?"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Message updated successfully",
  "result": {
    "id": 1,
    "content": "Hello! How are you doing?",
    "updatedAt": "2025-11-25T01:35:00",
    ...
  }
}
```

---

#### 1.6 Delete Message
**DELETE** `/api/messages/{messageId}`

Soft delete a message (only sender can delete).

**Response:**
```json
{
  "code": 200,
  "message": "Message deleted successfully",
  "result": null
}
```

---

## 2. WebSocket - Real-time Chat (project-service)

### WebSocket Endpoint
```
ws://localhost:8082/ws
```

### Connection Setup

#### JavaScript/TypeScript Example
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// Connect to WebSocket
const socket = new SockJS('http://localhost:8082/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log('Connected: ' + frame);
  
  // Subscribe to public chat
  stompClient.subscribe('/topic/public', (message) => {
    const chatMessage = JSON.parse(message.body);
    console.log('Received:', chatMessage);
  });
  
  // Subscribe to private messages
  stompClient.subscribe('/queue/private/' + userId, (message) => {
    const chatMessage = JSON.parse(message.body);
    console.log('Private message:', chatMessage);
  });
  
  // Subscribe to typing indicators
  stompClient.subscribe('/queue/typing/' + userId, (message) => {
    const typing = JSON.parse(message.body);
    console.log('Typing:', typing);
  });
});
```

---

### WebSocket Destinations

#### 2.1 Public Chat (Broadcast to All)

**Send to:** `/app/chat.sendMessage`  
**Receive from:** `/topic/public`

**Send Message:**
```javascript
stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John Doe',
  senderAvatarUrl: 'https://example.com/avatar.jpg',
  content: 'Hello everyone!'
}));
```

**Receive Message:**
```json
{
  "type": "CHAT",
  "senderId": 1,
  "senderName": "John Doe",
  "senderAvatarUrl": "https://example.com/avatar.jpg",
  "content": "Hello everyone!",
  "timestamp": "2025-11-25T01:30:00"
}
```

---

#### 2.2 Private Chat (One-to-One)

**Send to:** `/app/chat.private`  
**Receive from:** `/queue/private/{userId}`

**Send Private Message:**
```javascript
stompClient.send('/app/chat.private', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John Doe',
  senderAvatarUrl: 'https://example.com/avatar.jpg',
  receiverId: 2,
  content: 'Hi Jane, how are you?'
}));
```

**Receive Private Message:**
```json
{
  "type": "CHAT",
  "senderId": 1,
  "senderName": "John Doe",
  "receiverId": 2,
  "content": "Hi Jane, how are you?",
  "timestamp": "2025-11-25T01:30:00"
}
```

---

#### 2.3 Typing Indicator

**Send to:** `/app/chat.typing`  
**Receive from:** `/queue/typing/{userId}`

**Send Typing Status:**
```javascript
stompClient.send('/app/chat.typing', {}, JSON.stringify({
  userId: 1,
  userName: 'John Doe',
  receiverId: 2,
  isTyping: true
}));
```

**Receive Typing Status:**
```json
{
  "userId": 1,
  "userName": "John Doe",
  "receiverId": 2,
  "isTyping": true
}
```

---

#### 2.4 User Join/Leave

**Send to:** `/app/chat.addUser`  
**Receive from:** `/topic/public`

**Join Chat:**
```javascript
stompClient.send('/app/chat.addUser', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John Doe',
  senderAvatarUrl: 'https://example.com/avatar.jpg'
}));
```

**Receive Join/Leave Event:**
```json
{
  "type": "JOIN",  // or "LEAVE"
  "senderId": 1,
  "senderName": "John Doe",
  "timestamp": "2025-11-25T01:30:00"
}
```

---

#### 2.5 Project-Specific Chat

**Send to:** `/app/chat.project`  
**Receive from:** `/topic/project/{projectId}`

**Send Project Message:**
```javascript
// Subscribe to project chat
stompClient.subscribe('/topic/project/123', (message) => {
  const chatMessage = JSON.parse(message.body);
  console.log('Project message:', chatMessage);
});

// Send message to project
stompClient.send('/app/chat.project', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John Doe',
  projectId: 123,
  content: 'Let\'s work on this together!'
}));
```

---

## 3. Message Types

### ChatMessage Types
- `JOIN`: User joined the chat
- `LEAVE`: User left the chat
- `CHAT`: Regular chat message
- `TYPING`: User is typing indicator

---

## 4. Complete React Example

```jsx
import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function ChatComponent({ currentUser, receiverUser }) {
  const [stompClient, setStompClient] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);

  useEffect(() => {
    // Connect to WebSocket
    const socket = new SockJS('http://localhost:8082/ws');
    const client = Stomp.over(socket);

    client.connect({}, () => {
      console.log('Connected to WebSocket');

      // Subscribe to private messages
      client.subscribe(`/queue/private/${currentUser.id}`, (message) => {
        const chatMessage = JSON.parse(message.body);
        setMessages(prev => [...prev, chatMessage]);
      });

      // Subscribe to typing indicators
      client.subscribe(`/queue/typing/${currentUser.id}`, (message) => {
        const typing = JSON.parse(message.body);
        setIsTyping(typing.isTyping);
      });

      // Join the chat
      client.send('/app/chat.addUser', {}, JSON.stringify({
        senderId: currentUser.id,
        senderName: currentUser.name,
        senderAvatarUrl: currentUser.avatar
      }));
    });

    setStompClient(client);

    return () => {
      if (client) client.disconnect();
    };
  }, [currentUser]);

  const sendMessage = () => {
    if (stompClient && messageInput.trim()) {
      stompClient.send('/app/chat.private', {}, JSON.stringify({
        senderId: currentUser.id,
        senderName: currentUser.name,
        senderAvatarUrl: currentUser.avatar,
        receiverId: receiverUser.id,
        content: messageInput
      }));
      setMessageInput('');
    }
  };

  const handleTyping = () => {
    if (stompClient) {
      stompClient.send('/app/chat.typing', {}, JSON.stringify({
        userId: currentUser.id,
        userName: currentUser.name,
        receiverId: receiverUser.id,
        isTyping: true
      }));
    }
  };

  return (
    <div>
      <div className="messages">
        {messages.map((msg, idx) => (
          <div key={idx}>
            <strong>{msg.senderName}:</strong> {msg.content}
          </div>
        ))}
        {isTyping && <div>User is typing...</div>}
      </div>
      <input
        value={messageInput}
        onChange={(e) => setMessageInput(e.target.value)}
        onKeyPress={handleTyping}
      />
      <button onClick={sendMessage}>Send</button>
    </div>
  );
}
```

---

## 5. Testing with Postman

### REST API Testing
1. Import the endpoints into Postman
2. Set Authorization header with Bearer token
3. Test CRUD operations

### WebSocket Testing
1. Use Postman WebSocket feature
2. Connect to `ws://localhost:8082/ws`
3. Send STOMP frames manually

---

## 6. Security Notes

- All REST endpoints require authentication (JWT token)
- WebSocket connections should include authentication in headers
- Messages can only be updated/deleted by the sender
- Private messages are only visible to sender and receiver

---

## 7. Database Schema

### Message Table
```sql
CREATE TABLE message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

---

## Support

For issues or questions, please contact the development team.
