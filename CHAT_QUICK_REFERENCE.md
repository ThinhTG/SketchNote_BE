# 🚀 Chat Quick Reference

## REST API Endpoints (identity-service:8081)

### Send Message
```bash
POST /api/messages
Authorization: Bearer {token}
{
  "receiverId": 2,
  "content": "Hello!"
}
```

### Get Conversation
```bash
GET /api/messages/conversation/{userId}?page=0&size=20
Authorization: Bearer {token}
```

### Get All Conversations
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

## WebSocket (project-service:8082)

### Connect
```javascript
const socket = new SockJS('http://localhost:8082/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log('Connected');
});
```

### Public Chat
```javascript
// Subscribe
stompClient.subscribe('/topic/public', (message) => {
  const msg = JSON.parse(message.body);
});

// Send
stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John',
  content: 'Hello everyone!'
}));
```

### Private Chat
```javascript
// Subscribe
stompClient.subscribe('/queue/private/' + userId, (message) => {
  const msg = JSON.parse(message.body);
});

// Send
stompClient.send('/app/chat.private', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John',
  receiverId: 2,
  content: 'Hi Jane!'
}));
```

### Typing Indicator
```javascript
// Subscribe
stompClient.subscribe('/queue/typing/' + userId, (message) => {
  const typing = JSON.parse(message.body);
});

// Send
stompClient.send('/app/chat.typing', {}, JSON.stringify({
  userId: 1,
  userName: 'John',
  receiverId: 2,
  isTyping: true
}));
```

### Join Chat
```javascript
stompClient.send('/app/chat.addUser', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John'
}));
```

### Project Chat
```javascript
// Subscribe
stompClient.subscribe('/topic/project/' + projectId, (message) => {
  const msg = JSON.parse(message.body);
});

// Send
stompClient.send('/app/chat.project', {}, JSON.stringify({
  senderId: 1,
  senderName: 'John',
  projectId: 123,
  content: 'Project message'
}));
```

---

## Message Types

- `JOIN` - User joined
- `LEAVE` - User left  
- `CHAT` - Regular message
- `TYPING` - Typing indicator

---

## Testing

1. Open `chat-test-client.html`
2. Set User ID and Name
3. Click Connect
4. Start chatting!

For 2 users: Open in 2 browser windows with different User IDs.
