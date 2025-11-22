# Comment API Documentation

## Overview
Complete REST API for blog comments with Facebook-style nested comments, pagination, soft delete, and full CRUD operations.

## Base URL
```
/api/blogs
```

## Endpoints

### 1. Create Comment (Root or Reply)
**POST** `/api/blogs/{blogId}/comments`

Create a new comment on a blog post. Can be a root comment or a reply to another comment.

**Request Body:**
```json
{
  "content": "This is a comment",
  "parentCommentId": null  // null for root comment, or ID for reply
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "postId": 123,
  "content": "This is a comment",
  "authorId": 456,
  "authorDisplay": "John Doe",
  "authorAvatarUrl": "https://...",
  "parentCommentId": null,
  "replyCount": 0,
  "createdAt": "2025-11-22T16:30:00",
  "updatedAt": "2025-11-22T16:30:00",
  "isDeleted": false
}
```

---

### 2. Get Paginated Root Comments
**GET** `/api/blogs/{blogId}/comments?page=0&size=10`

Get paginated root comments for a blog post (comments with no parent).

**Query Parameters:**
- `page` (optional, default: 0) - Page number (0-indexed)
- `size` (optional, default: 10) - Number of comments per page

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "postId": 123,
      "content": "This is a comment",
      "authorId": 456,
      "authorDisplay": "John Doe",
      "authorAvatarUrl": "https://...",
      "parentCommentId": null,
      "replyCount": 3,
      "createdAt": "2025-11-22T16:30:00",
      "updatedAt": "2025-11-22T16:30:00",
      "isDeleted": false
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

---

### 3. Get Paginated Replies
**GET** `/api/blogs/comments/{parentId}/replies?page=0&size=5`

Get paginated replies for a specific comment (Facebook-style nested comments).

**Query Parameters:**
- `page` (optional, default: 0) - Page number (0-indexed)
- `size` (optional, default: 5) - Number of replies per page

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 2,
      "postId": 123,
      "content": "This is a reply",
      "authorId": 789,
      "authorDisplay": "Jane Smith",
      "authorAvatarUrl": "https://...",
      "parentCommentId": 1,
      "replyCount": 0,
      "createdAt": "2025-11-22T16:35:00",
      "updatedAt": "2025-11-22T16:35:00",
      "isDeleted": false
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 5,
  "hasNext": false,
  "hasPrevious": false
}
```

---

### 4. Update Comment
**PUT** `/api/blogs/comments/{commentId}`

Update the content of a comment. Only the author can update their own comments.

**Request Body:**
```json
{
  "content": "Updated comment content"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "postId": 123,
  "content": "Updated comment content",
  "authorId": 456,
  "authorDisplay": "John Doe",
  "authorAvatarUrl": "https://...",
  "parentCommentId": null,
  "replyCount": 3,
  "createdAt": "2025-11-22T16:30:00",
  "updatedAt": "2025-11-22T16:45:00",
  "isDeleted": false
}
```

**Error Response:** `403 Forbidden` (if not the author)

---

### 5. Delete Comment (Soft Delete)
**DELETE** `/api/blogs/comments/{commentId}`

Soft delete a comment. Only the author can delete their own comments. The comment is not removed from the database but marked as deleted.

**Response:** `204 No Content`

**Error Response:** `403 Forbidden` (if not the author)

---

### 6. Get Reply Count
**GET** `/api/blogs/comments/{commentId}/reply-count`

Get the number of replies for a specific comment.

**Response:** `200 OK`
```json
3
```

---

## Legacy Endpoints (Deprecated)

### Get All Root Comments (Non-Paginated)
**GET** `/api/blogs/posts/{postId}/comments`

⚠️ **Deprecated**: Use `GET /api/blogs/{blogId}/comments` with pagination instead.

### Get All Replies (Non-Paginated)
**GET** `/api/blogs/comments/{commentId}/replies-legacy`

⚠️ **Deprecated**: Use `GET /api/blogs/comments/{parentId}/replies` with pagination instead.

---

## Features

### ✅ Pagination
- Root comments: Default 10 per page, sorted by newest first
- Replies: Default 5 per page, sorted by oldest first (chronological)
- Customizable page size via query parameters

### ✅ Reply Count
- Each comment includes `replyCount` field
- Dynamically calculated, excluding soft-deleted replies
- Useful for "Show X replies" UI elements

### ✅ Soft Delete
- Comments are never hard-deleted from the database
- Deleted comments are marked with `deletedAt` timestamp
- Automatically filtered from all queries via Hibernate `@Where` annotation
- Maintains data integrity and referential relationships

### ✅ Authorization
- Only comment authors can update or delete their comments
- Authorization checked via Keycloak user ID
- Returns `403 Forbidden` for unauthorized attempts

### ✅ Validation
- Comment content is required (not blank)
- Content length: 1-5000 characters
- Validation errors return `400 Bad Request`

### ✅ Facebook-Style Nested Comments
- Two-level comment structure (root + replies)
- Parent comment validation ensures replies link to existing comments
- Paginated replies for better performance with many replies

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2025-11-22T16:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Comment content cannot be blank"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-11-22T16:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You are not authorized to update this comment"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-22T16:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Comment not found or has been deleted"
}
```

---

## Database Schema

### Comment Table
```sql
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blog_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT,
    parent_comment_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (blog_id) REFERENCES blog(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    
    INDEX idx_blog_id (blog_id),
    INDEX idx_parent_comment_id (parent_comment_id),
    INDEX idx_deleted_at (deleted_at)
);
```

---

## Usage Examples

### Create a root comment
```bash
curl -X POST http://localhost:8080/api/blogs/123/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"content": "Great article!"}'
```

### Create a reply
```bash
curl -X POST http://localhost:8080/api/blogs/123/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"content": "Thanks!", "parentCommentId": 1}'
```

### Get paginated comments
```bash
curl http://localhost:8080/api/blogs/123/comments?page=0&size=10 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get paginated replies
```bash
curl http://localhost:8080/api/blogs/comments/1/replies?page=0&size=5 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Update a comment
```bash
curl -X PUT http://localhost:8080/api/blogs/comments/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"content": "Updated content"}'
```

### Delete a comment
```bash
curl -X DELETE http://localhost:8080/api/blogs/comments/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```
