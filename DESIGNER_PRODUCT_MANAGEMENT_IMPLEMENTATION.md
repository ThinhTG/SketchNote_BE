# Designer Product Management - Implementation Guide

## Overview
Tài liệu này mô tả cách triển khai hệ thống quản lý sản phẩm cho Designer theo use case "Edit, Manage Resource Version".

## Architecture

### Entity Relationships
```
ResourceTemplate (1) ----< (N) ResourceTemplateVersion
    |
    ├── currentPublishedVersionId (references latest published version)
    ├── isArchived
    └── designer_id

ResourceTemplateVersion (1) ----< (N) ResourceTemplateVersionImage
ResourceTemplateVersion (1) ----< (N) ResourceTemplateVersionItem
```

### Key Entities

#### ResourceTemplate
- `templateId` - Primary key
- `designerId` - Owner
- `currentPublishedVersionId` - Tham chiếu đến version published hiện tại
- `isArchived` - True khi designer archive sản phẩm
- `status` - PENDING_REVIEW, PUBLISHED, REJECTED

#### ResourceTemplateVersion (NEW)
- `versionId` - Primary key
- `templateId` - Foreign key
- `versionNumber` - "1.0", "2.0", etc.
- `status` - PENDING_REVIEW, PUBLISHED, REJECTED
- `createdBy` - designer_id
- `reviewedBy` - staff_id
- `reviewComment` - Lý do reject hoặc comment

#### ResourceTemplateVersionImage (NEW)
- Lưu images của version

#### ResourceTemplateVersionItem (NEW)
- Lưu items của version

## Flow Diagram

### Creating New Version (UC: Edit Resource)
```
Designer Create New Version
    ↓
Validate Metadata
    ├─ Name required ✓
    ├─ Price > 0 ✓
    ├─ Release Date >= today ✓
    ├─ Expiration Date (if set) > Release Date ✓
    ↓
Auto-generate Version Number (1.0 → 2.0 → 3.0)
    ↓
System Creates ResourceTemplateVersion
    ├─ Status: PENDING_REVIEW
    ├─ createdBy: designer_id
    ├─ createdAt: now
    ↓
Store in DB + Pending Review Queue
    ↓
Designer Notified: "Version created and submitted for review"
    ↓
Existing Customers: Keep using old version (v1.0)
New Customers: See old version until new one is approved
    ↓
[Admin Review Process - Not in scope of this UC]
    ↓
If APPROVED:
    → Version Status: PUBLISHED
    → currentPublishedVersionId updated
    → New customers see v2.0
    → Old customers keep v1.0
    
If REJECTED:
    → Version Status: REJECTED
    → Designer can edit & republish
```

### Managing Product (View & Archive)
```
Designer View Products
    ↓
GET /api/orders/designer/products
    ├─ Retrieve all templates by designer_id
    ├─ Fetch all versions for each template
    ├─ Calculate statistics (purchases, revenue)
    ├─ Include current published version info
    ↓
Display:
    ├─ Product name, type, price, status
    ├─ Total purchases (30 days)
    ├─ Total revenue (30 days)
    ├─ Archive status
    ├─ Current published version
    ├─ List of all versions

Designer Archive Product
    ↓
POST /api/orders/designer/products/{id}/archive
    ├─ Set isArchived = true
    ├─ Product hidden from new customers
    ├─ Old customers keep access
    ↓
Designer Unarchive
    → Set isArchived = false
    → Product visible again
```

## API Endpoints

### 1. View Products
```
GET /api/orders/designer/products?page=0&size=10&sortBy=createdAt&sortDir=desc

Response:
{
  "content": [DesignerProductDTO, ...],
  "currentPage": 0,
  "totalPages": 5,
  ...
}
```

### 2. View Product Detail
```
GET /api/orders/designer/products/{resourceTemplateId}

Response: DesignerProductDTO (with all versions)
```

### 3. Create New Version
```
POST /api/orders/designer/products/{resourceTemplateId}/versions

Body:
{
  "sourceType": "UPLOAD" | "PROJECT",
  "projectId": 123,  // if sourceType = PROJECT
  "name": "Product Name v2",
  "description": "Description",
  "type": "ICONS",
  "price": 29.99,
  "releaseDate": "2025-12-06",
  "expiredTime": "2026-12-06",
  "images": [...],
  "items": [...]
}

Response: ResourceTemplateVersionDTO (status: PENDING_REVIEW)
```

### 4. Update Version (PENDING_REVIEW only)
```
PUT /api/orders/designer/products/versions/{versionId}

Body: Partial update of CreateResourceVersionDTO

Response: Updated ResourceTemplateVersionDTO
```

### 5. Archive/Unarchive
```
POST /api/orders/designer/products/{id}/archive
POST /api/orders/designer/products/{id}/unarchive

Response: DesignerProductDTO
```

### 6. View Versions
```
GET /api/orders/designer/products/{id}/versions?page=0&size=10

Response: PagedResponseDTO<ResourceTemplateVersionDTO>
```

### 7. Delete Version (PENDING_REVIEW only)
```
DELETE /api/orders/designer/products/versions/{versionId}

Response: Success message
```

## Version Number Generation

Algorithm:
```java
private String calculateNextVersionNumber(Optional<ResourceTemplateVersion> lastVersion) {
    if (lastVersion.isEmpty()) {
        return "1.0";
    }
    
    String currentVersion = lastVersion.get().getVersionNumber();
    String[] parts = currentVersion.split("\\.");
    
    if (parts.length == 2) {
        int major = Integer.parseInt(parts[0]);
        return (major + 1) + ".0";
    }
    
    return "1.0";
}
```

Example:
- First creation: 1.0
- First update: 2.0
- Second update: 3.0
- etc.

## Statistics Calculation

```
totalPurchases = SUM(order quantities from all versions, last 30 days)
totalRevenue = SUM(order subtotal amounts from all versions, last 30 days)
averageRating = AVG(feedback ratings for all versions)
```

Query:
```sql
SELECT 
  COALESCE(SUM(od.subtotal_amount), 0) as totalRevenue,
  COUNT(DISTINCT o.order_id) as totalPurchases
FROM orders o
JOIN order_details od ON o.order_id = od.order_id
JOIN resource_template rt ON od.resource_template_id = rt.template_id
WHERE rt.designer_id = ?
  AND rt.template_id IN (
    SELECT template_id FROM resource_template_version 
    WHERE template_id = ?
  )
  AND o.payment_status = 'PAID'
  AND o.order_status = 'SUCCESS'
  AND o.issue_date BETWEEN ? AND ?
```

## Security & Validation

### Authentication
- Tất cả endpoints yêu cầu valid JWT token
- Token phải belong to DESIGNER role

### Authorization
- Designer chỉ có thể:
  - View/edit/delete sản phẩm mình tạo
  - Archive/unarchive sản phẩm mình tạo
  - View versions của sản phẩm mình tạo

### Validation Rules
1. **Create/Update Version:**
   - Name: Required, không rỗng (max 50 chars)
   - Price: Required, > 0
   - Release Date: Required, >= today
   - Expiration Date: Optional, phải > releaseDate nếu có

2. **Edit Version:**
   - Chỉ có thể edit nếu status = PENDING_REVIEW
   - Tất cả field optional

3. **Delete Version:**
   - Chỉ có thể xóa nếu status = PENDING_REVIEW

4. **Archive:**
   - Có thể archive bất kỳ sản phẩm nào
   - Không ảnh hưởng đến old customers

## Database Migration

File: `V6__Create_resource_template_version_tables.sql`

Tables created:
1. `resource_template_version`
2. `resource_template_version_image`
3. `resource_template_version_item`

Modifications:
- Add `current_published_version_id` column to `resource_template`
- Add `is_archived` column to `resource_template`

## Implementation Checklist

### Backend
- [x] Create ResourceTemplateVersion entity
- [x] Create ResourceTemplateVersionImage entity
- [x] Create ResourceTemplateVersionItem entity
- [x] Create ResourceTemplateVersionDTO
- [x] Create CreateResourceVersionDTO
- [x] Create DesignerProductDTO
- [x] Create ResourceTemplateVersionRepository
- [x] Create DesignerResourceService interface
- [x] Create DesignerResourceServiceImpl
- [x] Create DesignerResourceController
- [x] Update OrderMapper for version mapping
- [x] Update ResourceTemplate entity (add columns)
- [x] Create migration SQL file
- [ ] Add unit tests
- [ ] Add integration tests
- [ ] Add API documentation (Swagger)

### Frontend (Next Steps)
- [ ] Create Designer Product Management page
- [ ] Create Product List component
- [ ] Create Product Detail component (with versions)
- [ ] Create Create/Edit Version form
- [ ] Add Archive/Unarchive buttons
- [ ] Add Delete version confirmation
- [ ] Add Statistics display (purchases, revenue)
- [ ] Add version history view
- [ ] Add status badge (PENDING_REVIEW, PUBLISHED, REJECTED)
- [ ] Add error handling & notifications

### Testing
- [ ] Test version number generation (1.0 → 2.0 → 3.0)
- [ ] Test backward compatibility (old customers keep old version)
- [ ] Test archive/unarchive logic
- [ ] Test permission checks (designer can only manage own products)
- [ ] Test validation rules (price > 0, date validation)
- [ ] Test statistics calculation

## Usage Examples

### Example 1: Create New Version
```bash
curl -X POST http://localhost:8888/api/orders/designer/products/1/versions \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceType": "UPLOAD",
    "name": "Icon Set v2",
    "description": "Updated with new icons",
    "type": "ICONS",
    "price": 29.99,
    "releaseDate": "2025-12-06",
    "images": [
      {
        "imageUrl": "https://...",
        "isThumbnail": true
      }
    ]
  }'
```

Response:
```json
{
  "success": true,
  "message": "New version created and submitted for review",
  "result": {
    "versionId": 5,
    "versionNumber": "2.0",
    "status": "PENDING_REVIEW",
    ...
  }
}
```

### Example 2: View All Products
```bash
curl http://localhost:8888/api/orders/designer/products \
  -H "Authorization: Bearer <token>"
```

### Example 3: Archive Product
```bash
curl -X POST http://localhost:8888/api/orders/designer/products/1/archive \
  -H "Authorization: Bearer <token>"
```

## Future Enhancements

1. **Bulk Operations**
   - Archive multiple products at once
   - Batch update pricing

2. **Advanced Statistics**
   - Per-version purchase analytics
   - Customer demographic data
   - Feedback sentiment analysis

3. **Automation**
   - Automatic expiration notifications
   - Price change suggestions based on demand
   - Auto-publish on schedule

4. **Collaboration**
   - Allow designers to comment on rejections
   - Reviewer feedback email notifications
   - Version comparison tool

5. **Export Features**
   - Export sales data as CSV
   - Revenue report generation
   - Analytics dashboard

## Troubleshooting

### Issue: Version number not incrementing
**Cause:** Query not fetching last version correctly
**Solution:** Check `findLastVersionByTemplateId` query uses correct ordering

### Issue: Old customers can't access old version
**Cause:** ResourceTemplate.currentPublishedVersionId not being set/used correctly
**Solution:** Verify order service uses correct version ID when processing orders

### Issue: Archive doesn't hide product
**Cause:** Frontend/query not filtering by isArchived
**Solution:** Add `isArchived = false` filter to public template queries

## Performance Considerations

1. **Indexes:**
   - `resource_template_version(template_id, status)`
   - `resource_template_version(created_by, status)`
   - `resource_template_version(template_id, created_at DESC)`

2. **Query Optimization:**
   - Use pagination for large result sets
   - Load versions on-demand (not eager load by default)
   - Cache designer product list (5-10 min TTL)

3. **Database:**
   - Partition `resource_template_version` by year if > 10M rows
   - Archive old versions to separate table if needed

---

## Contact & Support
For questions or issues, contact the backend development team.
