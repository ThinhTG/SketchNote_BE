# Designer Product Management - Quick Start Guide

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (for database)
- IntelliJ IDEA or VS Code

### 1. Database Setup

Run the migration script:
```bash
# This will be applied automatically on application startup
# File: V6__Create_resource_template_version_tables.sql

-- Or manually execute:
mysql -u root -p < identity-service/src/main/resources/db/migration/V6__Create_resource_template_version_tables.sql
```

### 2. Build the Project

```bash
cd order-service
mvn clean install -DskipTests
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

Application starts on: `http://localhost:8080` (direct) or `http://localhost:8888/api/orders` (via Gateway)

## 📚 API Quick Reference

### Base URL
```
http://localhost:8888/api/orders/designer/products
```

### Authentication
```bash
# Get JWT token first
curl -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "designer@example.com", "password": "password"}'

# Use token in header
-H "Authorization: Bearer <token>"
```

### 1️⃣ View My Products
```bash
curl http://localhost:8888/api/orders/designer/products \
  -H "Authorization: Bearer <token>"
```

Response: Paginated list of your products with statistics

### 2️⃣ Create New Version
```bash
curl -X POST http://localhost:8888/api/orders/designer/products/1/versions \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceType": "UPLOAD",
    "name": "Product Name v2",
    "description": "Updated description",
    "type": "ICONS",
    "price": 29.99,
    "releaseDate": "2025-12-06",
    "images": [
      {
        "imageUrl": "https://example.com/img.png",
        "isThumbnail": true
      }
    ]
  }'
```

### 3️⃣ Archive Product
```bash
curl -X POST http://localhost:8888/api/orders/designer/products/1/archive \
  -H "Authorization: Bearer <token>"
```

### 4️⃣ View All Versions of a Product
```bash
curl http://localhost:8888/api/orders/designer/products/1/versions \
  -H "Authorization: Bearer <token>"
```

### 5️⃣ Update Version (PENDING_REVIEW only)
```bash
curl -X PUT http://localhost:8888/api/orders/designer/products/versions/5 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "price": 39.99
  }'
```

### 6️⃣ Delete Version (PENDING_REVIEW only)
```bash
curl -X DELETE http://localhost:8888/api/orders/designer/products/versions/5 \
  -H "Authorization: Bearer <token>"
```

## 🗂️ Project Structure

```
order-service/
├── src/main/java/com/sketchnotes/order_service/
│   ├── entity/
│   │   ├── ResourceTemplate.java
│   │   ├── ResourceTemplateVersion.java ← NEW
│   │   ├── ResourceTemplateVersionImage.java ← NEW
│   │   ├── ResourceTemplateVersionItem.java ← NEW
│   │   └── ...
│   ├── dtos/
│   │   ├── designer/ ← NEW FOLDER
│   │   │   ├── DesignerProductDTO.java
│   │   │   ├── ResourceTemplateVersionDTO.java
│   │   │   └── CreateResourceVersionDTO.java
│   │   └── ...
│   ├── repository/
│   │   ├── ResourceTemplateVersionRepository.java ← NEW
│   │   └── ...
│   ├── service/
│   │   ├── designer/ ← NEW FOLDER
│   │   │   ├── DesignerResourceService.java
│   │   │   └── impl/
│   │   │       └── DesignerResourceServiceImpl.java
│   │   └── ...
│   ├── controller/
│   │   ├── DesignerResourceController.java ← NEW
│   │   └── ...
│   └── ...
├── src/main/resources/
│   ├── db/migration/
│   │   └── V6__Create_resource_template_version_tables.sql ← NEW
│   └── ...
└── ...
```

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=DesignerResourceServiceTest
```

### Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
# Report: target/site/jacoco/index.html
```

## 🐛 Debugging

### Enable Debug Logging
Add to `application.yml`:
```yaml
logging:
  level:
    com.sketchnotes.order_service: DEBUG
    org.springframework.web: DEBUG
```

### Check Database Queries
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### Debug in IDE
1. Set breakpoint in code
2. Run: `mvn spring-boot:run -Dspring-boot.run.fork=false`
3. Attach debugger

## 📊 Database Queries

### View All Versions of a Product
```sql
SELECT * FROM resource_template_version 
WHERE template_id = 1
ORDER BY created_at DESC;
```

### View Pending Reviews
```sql
SELECT * FROM resource_template_version 
WHERE status = 'PENDING_REVIEW'
ORDER BY created_at DESC;
```

### Check Product Statistics
```sql
SELECT 
  rt.template_id,
  rt.name,
  COUNT(DISTINCT rtv.version_id) as version_count,
  SUM(od.subtotal_amount) as total_revenue
FROM resource_template rt
LEFT JOIN resource_template_version rtv ON rt.template_id = rtv.template_id
LEFT JOIN order_details od ON rt.template_id = od.resource_template_id
WHERE rt.designer_id = 123
GROUP BY rt.template_id;
```

## 🔍 Common Issues & Solutions

### Issue 1: "You don't have permission to view this product"
**Cause:** You're trying to access another designer's product
**Solution:** Only access products you created

### Issue 2: "You can only edit versions in PENDING_REVIEW status"
**Cause:** Trying to edit PUBLISHED or REJECTED version
**Solution:** Delete PENDING_REVIEW and create new version

### Issue 3: Version number not incrementing
**Cause:** Database migration not applied
**Solution:** Verify `V6__...` migration ran successfully

### Issue 4: Images not saving
**Cause:** Image URLs are invalid or inaccessible
**Solution:** Verify image URLs are valid HTTP(S) URLs

## 📖 Documentation

### Full API Documentation
See: `DESIGNER_PRODUCT_MANAGEMENT_API.md`

### Implementation Details
See: `DESIGNER_PRODUCT_MANAGEMENT_IMPLEMENTATION.md`

### Code Comments
All services and controllers have detailed JavaDoc comments

## 🚢 Deployment

### Docker Build
```bash
# From order-service directory
docker build -f Dockerfile -t order-service:1.0 .
```

### Docker Run
```bash
docker run -p 8080:8080 \
  -e MYSQL_HOST=mysql-service \
  -e SPRING_PROFILES_ACTIVE=prod \
  order-service:1.0
```

### Docker Compose
```bash
# From project root
docker-compose up order-service
```

## 📋 Checklist Before Deployment

- [ ] All tests passing: `mvn clean test`
- [ ] No compilation errors: `mvn clean compile`
- [ ] Database migration applied successfully
- [ ] JWT authentication working
- [ ] CORS configured correctly
- [ ] API Gateway routing working
- [ ] Docker image builds successfully
- [ ] Environment variables set correctly
- [ ] Logging configured properly
- [ ] Performance tests passed

## 🤝 Contributing

### Code Style
- Follow Google Java Style Guide
- Use meaningful variable names
- Add comments for complex logic
- Keep methods under 50 lines

### Commit Message
```
[FEATURE/BUG/REFACTOR] Brief description

Detailed explanation of changes
- Point 1
- Point 2

Related to #123
```

### Pull Request
1. Create feature branch: `git checkout -b feature/designer-products`
2. Make changes
3. Commit: `git commit -m "[FEATURE] Description"`
4. Push: `git push origin feature/designer-products`
5. Create PR with description

## 📞 Support

### Resources
- API Docs: Check Swagger UI at `http://localhost:8888/swagger-ui.html`
- Database Schema: See migration file `V6__...`
- Architecture: See `DESIGNER_PRODUCT_MANAGEMENT_IMPLEMENTATION.md`

### Common Questions

**Q: How do old customers keep access to old versions?**
A: When they purchase, the order stores the specific version ID they purchased. When they download, system returns that version.

**Q: Can I delete published versions?**
A: No, only PENDING_REVIEW versions can be deleted. Published versions are permanent for customer access.

**Q: How long does review take?**
A: That's handled by admin panel (outside this scope). Designer just submits and waits.

**Q: What happens when I archive a product?**
A: It stops showing for new customers, but existing customers keep their purchased versions forever.

**Q: Can I change price of published version?**
A: No, create a new version with new price. Old customers keep old price forever.

## 🎯 Next Steps

1. **Frontend Development**
   - Build Designer Dashboard
   - Create Product Management UI
   - Implement version upload form

2. **Testing**
   - Write unit tests for services
   - Integration tests for APIs
   - E2E tests for full flow

3. **Admin Panel**
   - Implement version review/approval
   - Add rejection comment functionality
   - Create admin dashboard for pending reviews

4. **Analytics**
   - Calculate statistics from order data
   - Build revenue dashboards
   - Export sales reports

---

**Last Updated**: December 6, 2025
**Version**: 1.0
**Status**: Ready for Development ✅
