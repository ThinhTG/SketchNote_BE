# Template Filtering - Exclude Owned & Purchased

## ✅ **Đã cập nhật logic query với Native SQL**

Bây giờ `getAllActiveTemplates` và `getPopularTemplates` sử dụng **Native SQL** để:
- ✅ **Search trước** (filter templates)
- ✅ **Phân trang sau** (pagination)
- ✅ **Performance tốt hơn** JPQL

---

## 🎯 **Logic Filtering**

### **Điều kiện loại trừ**:
1. **Templates user đã mua**: `template_id NOT IN (SELECT resource_template_id FROM user_resource WHERE user_id = ? AND active = true)`
2. **Templates user tạo ra**: `designer_id != userId`

### **Kết quả**: Chỉ hiển thị templates có thể mua

---

## 📋 **Repository Queries (Native SQL)**

### **1. findAvailableTemplatesForUser**
```java
@Query(value = "SELECT rt.* FROM resource_template rt " +
               "WHERE rt.status = :status " +
               "AND rt.designer_id != :userId " +
               "AND rt.template_id NOT IN (" +
               "    SELECT ur.resource_template_id FROM user_resource ur " +
               "    WHERE ur.user_id = :userId AND ur.active = true" +
               ") " +
               "ORDER BY rt.created_at DESC",
       countQuery = "SELECT COUNT(*) FROM resource_template rt " +
                    "WHERE rt.status = :status " +
                    "AND rt.designer_id != :userId " +
                    "AND rt.template_id NOT IN (" +
                    "    SELECT ur.resource_template_id FROM user_resource ur " +
                    "    WHERE ur.user_id = :userId AND ur.active = true" +
                    ")",
       nativeQuery = true)
Page<ResourceTemplate> findAvailableTemplatesForUser(
        @Param("status") String status,
        @Param("userId") Long userId,
        Pageable pageable);
```

**Sử dụng trong**: `getAllActiveTemplates()`

**Logic**:
- ✅ Status = 'PUBLISHED'
- ❌ Loại bỏ templates user tạo (`designer_id != userId`)
- ❌ Loại bỏ templates user đã mua (`NOT IN user_resource`)
- ✅ Sort by `created_at DESC`
- ✅ **Pagination** với `countQuery`

---

### **2. findPopularTemplatesForUser**
```java
@Query(value = "SELECT rt.*, COUNT(o.order_id) as order_count " +
               "FROM resource_template rt " +
               "LEFT JOIN \"order\" o ON o.resource_template_id = rt.template_id " +
               "    AND o.status = 'COMPLETED' " +
               "WHERE rt.status = :status " +
               "AND rt.designer_id != :userId " +
               "AND rt.template_id NOT IN (" +
               "    SELECT ur.resource_template_id FROM user_resource ur " +
               "    WHERE ur.user_id = :userId AND ur.active = true" +
               ") " +
               "GROUP BY rt.template_id, rt.name, rt.description, rt.price, " +
               "         rt.type, rt.designer_id, rt.status, rt.release_date, " +
               "         rt.created_at, rt.updated_at, rt.current_published_version_id " +
               "ORDER BY order_count DESC " +
               "LIMIT :limit",
       nativeQuery = true)
List<ResourceTemplate> findPopularTemplatesForUser(
        @Param("status") String status,
        @Param("userId") Long userId,
        @Param("limit") int limit);
```

**Sử dụng trong**: `getPopularTemplates()`

**Logic**:
- ✅ Status = 'PUBLISHED'
- ❌ Loại bỏ templates user tạo
- ❌ Loại bỏ templates user đã mua
- ✅ **LEFT JOIN** với Order table
- ✅ **GROUP BY** tất cả columns (PostgreSQL requirement)
- ✅ **ORDER BY** `COUNT(order_id)` DESC (popularity)
- ✅ **LIMIT** trực tiếp trong SQL

---

## 🔄 **Service Updates**

### **getAllActiveTemplates()**

**Trước**:
```java
Page<ResourceTemplate> templatePage = resourceTemplateRepository
    .findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
```

**Sau**:
```java
Page<ResourceTemplate> templatePage;

if (currentUserId != null) {
    // Logged in user: filter out owned & purchased
    templatePage = resourceTemplateRepository.findAvailableTemplatesForUser(
            ResourceTemplate.TemplateStatus.PUBLISHED, currentUserId, pageable);
} else {
    // Guest user: show all
    templatePage = resourceTemplateRepository.findByStatus(
            ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
}
```

---

### **getPopularTemplates()**

**Trước**:
```java
List<ResourceTemplate> templates = resourceTemplateRepository
    .findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED).stream()
    .sorted((t1, t2) -> t2.getPrice().compareTo(t1.getPrice()))
    .limit(limit)
    .toList();
```

**Sau**:
```java
List<ResourceTemplate> templates;

if (currentUserId != null) {
    // Logged in: filter + sort by popularity
    templates = resourceTemplateRepository.findPopularTemplatesForUser(
            ResourceTemplate.TemplateStatus.PUBLISHED, currentUserId, pageable);
} else {
    // Guest: show all sorted by price
    templates = resourceTemplateRepository
        .findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED).stream()
        .sorted((t1, t2) -> t2.getPrice().compareTo(t1.getPrice()))
        .limit(limit)
        .toList();
}
```

---

## 🧪 **Testing Scenarios**

### **Scenario 1: User chưa mua gì**
```
User ID: 5
Templates:
- Template 1 (owner: user 1) ✅ Show
- Template 2 (owner: user 2) ✅ Show
- Template 3 (owner: user 5) ❌ Hide (user là owner)
```

### **Scenario 2: User đã mua Template 1**
```
User ID: 5
UserResource: [Template 1]
Templates:
- Template 1 (owner: user 1) ❌ Hide (đã mua)
- Template 2 (owner: user 2) ✅ Show
- Template 3 (owner: user 5) ❌ Hide (user là owner)
```

### **Scenario 3: Guest user (không login)**
```
User ID: null
Templates:
- Template 1 ✅ Show
- Template 2 ✅ Show
- Template 3 ✅ Show
(Hiển thị tất cả PUBLISHED templates)
```

---

## 📊 **Database Query Example**

### **Find Available Templates**
```sql
SELECT rt.* 
FROM resource_template rt
WHERE rt.status = 'PUBLISHED'
  AND rt.designer_id != 5  -- Not owner
  AND rt.template_id NOT IN (
      SELECT ur.resource_template_id 
      FROM user_resource ur 
      WHERE ur.user_id = 5 AND ur.active = true  -- Not purchased
  );
```

### **Find Popular Templates**
```sql
SELECT rt.*, COUNT(o.order_id) as order_count
FROM resource_template rt
LEFT JOIN "order" o ON o.resource_template_id = rt.template_id 
                    AND o.status = 'COMPLETED'
WHERE rt.status = 'PUBLISHED'
  AND rt.designer_id != 5
  AND rt.template_id NOT IN (
      SELECT ur.resource_template_id 
      FROM user_resource ur 
      WHERE ur.user_id = 5 AND ur.active = true
  )
GROUP BY rt.template_id
ORDER BY COUNT(o.order_id) DESC
LIMIT 10;
```

---

## ✅ **Summary**

### **Changes Made**:
1. ✅ Added `findAvailableTemplatesForUser()` query
2. ✅ Added `findPopularTemplatesForUser()` query  
3. ✅ Updated `getAllActiveTemplates()` service method
4. ✅ Updated `getPopularTemplates()` service method
5. ✅ Removed unused `findByStatusAnd()` method

### **Benefits**:
- ✅ Users chỉ thấy templates có thể mua
- ✅ Không hiển thị templates đã sở hữu
- ✅ Không hiển thị templates tự tạo
- ✅ Guest users vẫn thấy tất cả templates
- ✅ Popular templates được sort theo số lượng orders thực tế

---

## 🚀 **API Behavior**

### **GET /api/templates?page=0&size=20**
- **Logged in**: Chỉ templates chưa mua & không phải owner
- **Guest**: Tất cả PUBLISHED templates

### **GET /api/templates/popular?limit=10**
- **Logged in**: Top 10 popular templates (chưa mua & không phải owner)
- **Guest**: Top 10 templates sorted by price

Perfect! Bây giờ marketplace chỉ hiển thị templates có thể mua! 🎉
