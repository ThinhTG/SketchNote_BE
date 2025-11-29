# Thêm Thống Kê cho API GET Resource Template

## Tóm tắt
Đã thêm 3 trường thống kê mới vào API GET resource template:
- **purchaseCount**: Số lượt mua (đếm từ bảng orders với status SUCCESS và payment PAID)
- **feedbackCount**: Số lượng feedback
- **averageRating**: Số sao trung bình (1-5)

## Các thay đổi đã thực hiện

### 1. Order Service

#### 1.1. ResourceTemplateDTO
**File**: `order-service/src/main/java/com/sketchnotes/order_service/dtos/ResourceTemplateDTO.java`
- Thêm 3 trường mới:
  - `private Long purchaseCount;`
  - `private Long feedbackCount;`
  - `private Double averageRating;`

#### 1.2. FeedbackStatsResponse DTO
**File**: `order-service/src/main/java/com/sketchnotes/order_service/dtos/FeedbackStatsResponse.java`
- Tạo DTO mới để nhận response từ identity-service
- Chứa `totalFeedbacks` và `averageRating`

#### 1.3. IdentityClient
**File**: `order-service/src/main/java/com/sketchnotes/order_service/client/IdentityClient.java`
- Thêm method `getFeedbackStats(@PathVariable Long resourceId)` để gọi API feedback từ identity-service

#### 1.4. OrderRepository
**File**: `order-service/src/main/java/com/sketchnotes/order_service/repository/OrderRepository.java`
- Thêm method `countSuccessfulOrdersByTemplateId(Long templateId)` để đếm số lượt mua thành công

#### 1.5. TemplateServiceImpl
**File**: `order-service/src/main/java/com/sketchnotes/order_service/service/implement/TemplateServiceImpl.java`

**Thêm dependencies**:
- `OrderRepository` để đếm số lượt mua
- `IdentityClient` để lấy feedback stats

**Thêm helper methods**:
- `populateStatistics(ResourceTemplateDTO dto)`: Populate stats cho 1 template
- `populateStatistics(List<ResourceTemplateDTO> dtos)`: Populate stats cho list templates

**Cập nhật các methods**:
Tất cả các method trả về `ResourceTemplateDTO` hoặc `List<ResourceTemplateDTO>` đều được cập nhật để gọi `populateStatistics()`:
- `getAllActiveTemplates(int page, int size, String sortBy, String sortDir)`
- `getTemplateById(Long id)`
- `getTemplatesByDesignerAndStatus(...)`
- `getTemplatesByType(...)`
- `searchTemplates(...)`
- `getTemplatesByPriceRange(...)`
- `getLatestTemplates(int limit)`
- `getPopularTemplates(int limit)`
- `getTemplatesByReviewStatus(...)`

### 2. Identity Service

#### 2.1. FeedbackController
**File**: `identity-service/src/main/java/com/sketchnotes/identityservice/controller/FeedbackController.java`
- Thêm endpoint mới: `GET /api/feedback/resource/{resourceId}/stats`
- Endpoint này trả về lightweight stats (chỉ count và average rating) để tối ưu performance khi load danh sách templates

## Cách hoạt động

1. Khi client gọi API GET resource template (ví dụ: `/api/orders/template`)
2. Service sẽ:
   - Lấy danh sách templates từ database
   - Map sang DTO
   - Với mỗi template, gọi `populateStatistics()`:
     - Đếm số lượt mua từ `OrderRepository`
     - Gọi `IdentityClient.getFeedbackStats()` để lấy feedback count và average rating
   - Trả về response với đầy đủ thông tin thống kê

## API Response Example

```json
{
  "code": 200,
  "message": "Fetched templates",
  "result": {
    "content": [
      {
        "resourceTemplateId": 1,
        "designerId": 10,
        "name": "Modern UI Kit",
        "description": "A beautiful modern UI kit",
        "type": "TEMPLATES",
        "price": 29.99,
        "purchaseCount": 156,
        "feedbackCount": 42,
        "averageRating": 4.5,
        "images": [...],
        "items": [...],
        "designerInfo": {...},
        "isOwner": false
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5
  }
}
```

## Performance Considerations

- Feedback stats được cache ở identity-service level
- Sử dụng lightweight endpoint `/stats` thay vì full feedback list
- Error handling: Nếu feedback service không available, sẽ trả về giá trị mặc định (0 và null)
- Có thể cân nhắc thêm caching ở order-service level nếu cần

## Testing

Để test các thay đổi:

1. **Test purchase count**:
   - Tạo một số orders thành công cho một template
   - Gọi API GET template và kiểm tra `purchaseCount`

2. **Test feedback stats**:
   - Tạo feedback cho template qua identity-service
   - Gọi API GET template và kiểm tra `feedbackCount` và `averageRating`

3. **Test error handling**:
   - Stop identity-service
   - Gọi API GET template và kiểm tra vẫn hoạt động (với feedback stats = 0/null)

## Next Steps (Optional)

1. **Caching**: Thêm Redis cache cho feedback stats để giảm load
2. **Batch Loading**: Sử dụng batch API để load feedback stats cho nhiều templates cùng lúc
3. **Async Loading**: Load stats asynchronously để không block main request
