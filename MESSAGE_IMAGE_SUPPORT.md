# Message API - Image Support

## ✅ Đã thêm trường `isImage` vào Message API

Bây giờ API hỗ trợ cả **text message** và **image message**!

---

## 📋 **API Changes**

### **1. Send Message API**

**Endpoint**: `POST /api/messages`

**Request Body**:
```json
{
  "receiverId": 123,
  "content": "Hello world!",
  "isImage": false
}
```

**Fields**:
- `receiverId` (Long, required): ID người nhận
- `content` (String, required): Nội dung tin nhắn
  - Nếu `isImage = false`: Text message
  - Nếu `isImage = true`: Image URL
- `isImage` (boolean, optional): Default = `false`
  - `false`: Tin nhắn text thường
  - `true`: Tin nhắn là ảnh (content chứa URL)

---

### **2. Response Format**

**MessageResponse**:
```json
{
  "id": 1,
  "senderId": 4,
  "senderName": "John Doe",
  "senderAvatarUrl": "https://...",
  "receiverId": 5,
  "receiverName": "Jane Smith",
  "receiverAvatarUrl": "https://...",
  "content": "Hello world!",
  "isImage": false,
  "createdAt": "2025-12-25T12:00:00",
  "updatedAt": "2025-12-25T12:00:00"
}
```

---

## 🎯 **Use Cases**

### **Case 1: Gửi text message**
```json
POST /api/messages
{
  "receiverId": 5,
  "content": "Hello! How are you?",
  "isImage": false
}
```

### **Case 2: Gửi image message**
```json
POST /api/messages
{
  "receiverId": 5,
  "content": "https://example.com/images/photo.jpg",
  "isImage": true
}
```

### **Case 3: Gửi image từ S3**
```json
POST /api/messages
{
  "receiverId": 5,
  "content": "https://s3.amazonaws.com/bucket/chat-images/abc123.png",
  "isImage": true
}
```

---

## 🔄 **Frontend Integration**

### **Hiển thị message**

```javascript
function renderMessage(message) {
  if (message.isImage) {
    // Hiển thị ảnh
    return `<img src="${message.content}" alt="Image message" />`;
  } else {
    // Hiển thị text
    return `<p>${message.content}</p>`;
  }
}
```

### **Gửi text message**

```javascript
async function sendTextMessage(receiverId, text) {
  const response = await fetch('/api/messages', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
      receiverId: receiverId,
      content: text,
      isImage: false
    })
  });
  return response.json();
}
```

### **Gửi image message**

```javascript
async function sendImageMessage(receiverId, imageUrl) {
  const response = await fetch('/api/messages', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
      receiverId: receiverId,
      content: imageUrl,  // URL của ảnh đã upload
      isImage: true
    })
  });
  return response.json();
}
```

### **Upload ảnh và gửi message**

```javascript
async function uploadAndSendImage(receiverId, imageFile) {
  // 1. Upload ảnh lên S3 hoặc server
  const formData = new FormData();
  formData.append('file', imageFile);
  
  const uploadResponse = await fetch('/api/upload', {
    method: 'POST',
    body: formData
  });
  const uploadData = await uploadResponse.json();
  const imageUrl = uploadData.url;
  
  // 2. Gửi message với image URL
  return sendImageMessage(receiverId, imageUrl);
}
```

---

## 🗄️ **Database Changes**

**Migration**: `V11__add_is_image_to_message.sql`

```sql
ALTER TABLE message ADD COLUMN IF NOT EXISTS is_image BOOLEAN NOT NULL DEFAULT FALSE;
```

**Message Table**:
```
message
├── id (BIGINT)
├── sender_id (BIGINT)
├── receiver_id (BIGINT)
├── content (TEXT)
├── is_image (BOOLEAN) ← NEW
├── created_at (TIMESTAMP)
├── updated_at (TIMESTAMP)
└── deleted_at (TIMESTAMP)
```

---

## 🧪 **Testing**

### **Test 1: Send text message**
```bash
curl -X POST https://sketchnote.litecsys.com/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "receiverId": 5,
    "content": "Hello!",
    "isImage": false
  }'
```

### **Test 2: Send image message**
```bash
curl -X POST https://sketchnote.litecsys.com/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "receiverId": 5,
    "content": "https://example.com/image.jpg",
    "isImage": true
  }'
```

### **Test 3: Get conversation**
```bash
curl -X GET "https://sketchnote.litecsys.com/api/messages/conversation/5?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Response sẽ có `isImage` field:
```json
{
  "content": [
    {
      "id": 1,
      "content": "Hello!",
      "isImage": false
    },
    {
      "id": 2,
      "content": "https://example.com/image.jpg",
      "isImage": true
    }
  ]
}
```

---

## 📝 **Notes**

1. **Default value**: Nếu không gửi `isImage`, mặc định là `false` (text message)
2. **Validation**: `content` vẫn required, min 1 max 10000 characters
3. **Image URL**: Khi `isImage = true`, `content` nên chứa valid URL
4. **Backward compatible**: Các message cũ sẽ có `isImage = false`

---

## 🚀 **Deployment**

1. **Run migration**: V11 sẽ tự động chạy khi restart service
2. **Restart identity-service**: Deploy code mới
3. **Test API**: Verify cả text và image messages

---

Bây giờ bạn có thể gửi cả text và image trong chat! 🎉
