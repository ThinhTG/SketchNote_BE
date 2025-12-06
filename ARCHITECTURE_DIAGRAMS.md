# Designer Product Management - Architecture Diagram

## Database Schema Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         resource_template                            │
├─────────────────────────────────────────────────────────────────────┤
│ PK  template_id (BIGINT)                                            │
│     designer_id (BIGINT)                                            │
│     name (VARCHAR 50)                                               │
│     description (VARCHAR 255)                                       │
│     type (ENUM: ICONS, TEMPLATES, FONT, ...)                        │
│     price (DECIMAL 15,2)                                            │
│     expired_time (DATE)                                             │
│     release_date (DATE)                                             │
│     status (ENUM: PENDING_REVIEW, PUBLISHED, REJECTED)              │
│     created_at (DATETIME)                                           │
│     updated_at (DATETIME)                                           │
│ NEW current_published_version_id (BIGINT) → Latest PUBLISHED       │
│ NEW is_archived (BOOLEAN, default: false)                           │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ 1
                              │
                              ├─────────────────────────────┐
                              │                             │
                              ▼ N                           ▼ N
            ┌──────────────────────────────────┐  ┌─────────────────────────┐
            │ resource_template_version (NEW)  │  │ resource_template_image │
            ├──────────────────────────────────┤  ├─────────────────────────┤
            │ PK version_id (BIGINT)           │  │ PK image_id (BIGINT)    │
            │    template_id (BIGINT, FK)      │  │    template_id (FK)     │
            │    version_number (VARCHAR 20)   │  │    image_url (VARCHAR)  │
            │    name (VARCHAR 50)             │  │    is_thumbnail (BOOL)  │
            │    description (VARCHAR 255)     │  └─────────────────────────┘
            │    type (ENUM)                   │
            │    price (DECIMAL 15,2)          │
            │    expired_time (DATE)           │
            │    release_date (DATE)           │
            │    status (ENUM)                 │  ┌──────────────────────────┐
            │    created_at (DATETIME)         │  │ resource_template_item   │
            │    updated_at (DATETIME)         │  │ (NEW)                    │
            │    created_by (BIGINT) ───────┐  │  ├──────────────────────────┤
            │    reviewed_by (BIGINT)        │  │  │ PK item_id (BIGINT)     │
            │    reviewed_at (DATETIME)      │  │  │    version_id (FK)      │
            │    review_comment (TEXT)       │  │  │    item_index (INT)     │
            └──────────────────────────────────┘  │    item_url (VARCHAR)   │
                              │                    │    image_url (VARCHAR)  │
                              │                    └──────────────────────────┘
                              ├─ references ────────┤
                              │  created_by: user ID (Designer)
                              │  reviewed_by: user ID (Admin/Staff)
```

## API Flow Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                      API Gateway (Port 8888)                         │
│                    /api/orders/designer/products                     │
└──────────────┬───────────────────────────────────────────────────────┘
               │
               │ Routes to
               ▼
┌──────────────────────────────────────────────────────────────────────┐
│                  Order Service (Port 8080)                           │
└─────────┬──────────────────────────────────────────────────────────┬─┘
          │                                                          │
          ▼                                                          ▼
┌──────────────────────────────┐                    ┌──────────────────────┐
│  DesignerResourceController  │                    │  Identity Service    │
├──────────────────────────────┤                    │  (Verify JWT Token)  │
│ GET    /products             │                    └──────────────────────┘
│ GET    /{id}                 │
│ POST   /{id}/versions        │
│ PUT    /versions/{id}        │
│ POST   /{id}/archive         │
│ POST   /{id}/unarchive       │
│ POST   /versions/{id}/...    │
│ DELETE /versions/{id}        │
│ GET    /{id}/versions        │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ DesignerResourceService      │
├──────────────────────────────┤
│ - getMyProducts()            │
│ - getProductDetail()         │
│ - createNewVersion()         │
│ - updateVersion()            │
│ - archiveProduct()           │
│ - deleteVersion()            │
│ - populateStatistics()       │
└────────────┬─────────────────┘
             │
             ├─────────────────┬──────────────────┐
             ▼                 ▼                  ▼
        ┌──────────┐    ┌──────────────────┐ ┌───────────┐
        │Resource  │    │Resource Template │ │Dashboard  │
        │Template  │    │Version           │ │Repository │
        │Repository│    │Repository        │ │(Stats)    │
        └──────────┘    └──────────────────┘ └───────────┘
             │                   │                  │
             └───────┬───────────┴──────────────────┘
                     ▼
          ┌──────────────────────┐
          │    MySQL Database    │
          │                      │
          │ - resource_template  │
          │ - resource_template_ │
          │   version            │
          │ - resource_template_ │
          │   version_image      │
          │ - resource_template_ │
          │   version_item       │
          └──────────────────────┘
```

## Service Layer Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                     DesignerResourceService                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. getMyProducts(designerId, pagination)                          │
│     ├─> Query templates by designerId                             │
│     ├─> For each template, get versions                           │
│     ├─> Calculate statistics (revenue, purchases)                 │
│     └─> Return DesignerProductDTO list                            │
│                                                                     │
│  2. createNewVersion(templateId, designerId, createDTO)           │
│     ├─> Verify designer ownership                                 │
│     ├─> Validate metadata (name, price, dates)                    │
│     ├─> Calculate next version number (1.0 → 2.0)                 │
│     ├─> Create ResourceTemplateVersion entity                     │
│     ├─> Attach images & items                                     │
│     ├─> Set status = PENDING_REVIEW                               │
│     ├─> Save to DB                                                │
│     └─> Return ResourceTemplateVersionDTO                         │
│                                                                     │
│  3. updateVersion(versionId, designerId, updateDTO)               │
│     ├─> Verify designer ownership                                 │
│     ├─> Check status = PENDING_REVIEW                             │
│     ├─> Update metadata fields (optional)                         │
│     ├─> Update images & items (optional)                          │
│     ├─> Save changes                                              │
│     └─> Return updated ResourceTemplateVersionDTO                 │
│                                                                     │
│  4. archiveProduct(templateId, designerId)                        │
│     ├─> Verify designer ownership                                 │
│     ├─> Set isArchived = true                                     │
│     ├─> Save template                                             │
│     └─> Return updated DesignerProductDTO                         │
│                                                                     │
│  5. deleteVersion(versionId, designerId)                          │
│     ├─> Verify designer ownership                                 │
│     ├─> Check status = PENDING_REVIEW                             │
│     ├─> Delete from DB                                            │
│     └─> Return success                                            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Version Lifecycle

```
                    ┌─────────────┐
                    │   Create    │
                    │  Version    │
                    └──────┬──────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │ PENDING_REVIEW   │
                  │                  │
                  │ Can:             │
                  │ - Edit           │
                  │ - Delete         │
                  │ - Republish      │
                  └────────┬─────────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
    Designer       Admin Review      Admin Review
    Delete/Edit      Approves         Rejects
            │              │              │
            │              ▼              ▼
            │      ┌──────────────┐  ┌──────────┐
            │      │  PUBLISHED   │  │REJECTED  │
            │      │              │  │          │
            │      │ Customers    │  │ Can:     │
            │      │ can see it   │  │ - Edit   │
            │      │ and buy      │  │ - Delete │
            │      │              │  │ - Retry  │
            │      └──────────────┘  └────┬─────┘
            │                             │
            │                    Resubmit │
            │                             │
            └─────────────┬───────────────┘
                          ▼
                   [Back to PENDING_REVIEW]

Legend:
→ Automatic transition
- Manual action
```

## Backward Compatibility Flow

```
Timeline:
─────────────────────────────────────────────────────

V1.0 Published
│
├─ Existing Customer 1 Buys ──┐
├─ Existing Customer 2 Buys ──┤─ Order stores version_id = 1
├─ Existing Customer 3 Buys ──┘
│
├─ Designer creates V2.0
│  (Auto-increments version number)
│  (Status: PENDING_REVIEW)
│
├─ Admin approves V2.0
│  (Status: PENDING_REVIEW → PUBLISHED)
│  (current_published_version_id = V2.0 id)
│
├─ NEW Customer A buys ────→ Gets V2.0
├─ NEW Customer B buys ────→ Gets V2.0
│
├─ On download:
│  ├─ Existing Customer 1 ──→ Downloads V1.0 (from order history)
│  ├─ Existing Customer 2 ──→ Downloads V1.0 (from order history)
│  ├─ NEW Customer A ───────→ Downloads V2.0 (current version)
│  └─ NEW Customer B ───────→ Downloads V2.0 (current version)
│
└─ Result: Perfect backward compatibility ✓
```

## Authentication & Authorization Flow

```
┌──────────────┐
│   Request    │
│ with JWT     │
└──────┬───────┘
       │
       ▼
┌─────────────────────────────────────┐
│ API Gateway Route Check             │
│ /api/orders/designer/products       │
└──────┬──────────────────────────────┘
       │
       ▼
┌────────────────────────────────────────┐
│ Controller: DesignerResourceController │
│ Extract JWT Token from Header          │
└──────┬─────────────────────────────────┘
       │
       ▼
┌────────────────────────────────────────┐
│ getCurrentDesignerId()                 │
│ Call: identityClient.getCurrentUser()  │
└──────┬─────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Identity Service Validates JWT      │
│ Returns: UserResponse with role     │
└──────┬──────────────────────────────┘
       │
       ├─ NOT AUTHENTICATED
       │  └─> 401 Unauthorized
       │
       ├─ ROLE ≠ DESIGNER
       │  └─> 403 Forbidden
       │
       └─ VALID DESIGNER
          │
          ▼
       ┌──────────────────────────────┐
       │ Call Service Layer           │
       │ with designerId              │
       └──────┬───────────────────────┘
              │
              ▼
       ┌──────────────────────────────┐
       │ Service: Verify Ownership    │
       │ template.designerId          │
       │ == designerId (from JWT)     │
       └──────┬───────────────────────┘
              │
              ├─ NO MATCH
              │  └─> 403 Forbidden
              │
              └─ MATCH
                 └─> Proceed with operation ✓
```

## Data Validation Pipeline

```
┌─────────────────────────────────────────────────────┐
│          CreateResourceVersionDTO                   │
│  (from request body)                                │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
    ┌──────────────────────────────────┐
    │ Input Validation (Controller)    │
    ├──────────────────────────────────┤
    │ ✓ name != null && not empty      │
    │ ✓ price != null && > 0           │
    │ ✓ releaseDate != null            │
    │ ✓ expiredTime > releaseDate      │
    └──────────┬───────────────────────┘
               │
               ├─ FAIL ──> 400 Bad Request
               │
               └─ PASS
                  │
                  ▼
    ┌──────────────────────────────────┐
    │ Business Logic Validation        │
    │ (Service)                        │
    ├──────────────────────────────────┤
    │ ✓ Designer owns product          │
    │ ✓ Version number unique          │
    │ ✓ Images URLs valid              │
    │ ✓ Status flow allowed            │
    └──────────┬───────────────────────┘
               │
               ├─ FAIL ──> 400/403/404
               │
               └─ PASS
                  │
                  ▼
    ┌──────────────────────────────────┐
    │ Database Persistence             │
    │ (Repository + JPA)               │
    ├──────────────────────────────────┤
    │ ✓ FK constraints                 │
    │ ✓ Unique constraints             │
    │ ✓ NOT NULL constraints           │
    └──────────┬───────────────────────┘
               │
               ├─ FAIL ──> 400/409
               │
               └─ PASS
                  │
                  ▼
           ┌─────────────┐
           │   Success   │
           │   201 OK    │
           └─────────────┘
```

---

**Note**: These diagrams show:
1. **Database Schema**: How data is structured
2. **API Flow**: How requests flow through layers
3. **Service Logic**: Business logic organization
4. **Version Lifecycle**: Version status transitions
5. **Backward Compatibility**: How old customers keep old versions
6. **Security**: Authentication & authorization checks
7. **Validation**: Multi-layer input validation

For implementation details, see `DESIGNER_PRODUCT_MANAGEMENT_IMPLEMENTATION.md`
