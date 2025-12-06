-- V1__Create_resource_template_version_tables.sql
-- Script này tạo các bảng cho versioning của Resource Template

-- 1. Thêm cột mới vào bảng resource_template
ALTER TABLE resource_template ADD COLUMN current_published_version_id BIGINT;
ALTER TABLE resource_template ADD COLUMN is_archived BOOLEAN DEFAULT false;

-- 2. Tạo bảng resource_template_version
CREATE TABLE resource_template_version (
    version_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL,
    version_number VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    type VARCHAR(50),
    price DECIMAL(15, 2) NOT NULL,
    expired_time DATE,
    release_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    review_comment TEXT,
    FOREIGN KEY (template_id) REFERENCES resource_template(template_id) ON DELETE CASCADE,
    UNIQUE KEY uq_template_version (template_id, version_number),
    INDEX idx_template_id (template_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tạo bảng resource_template_version_image
CREATE TABLE resource_template_version_image (
    image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    is_thumbnail BOOLEAN DEFAULT false,
    FOREIGN KEY (version_id) REFERENCES resource_template_version(version_id) ON DELETE CASCADE,
    INDEX idx_version_id (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tạo bảng resource_template_version_item
CREATE TABLE resource_template_version_item (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_id BIGINT NOT NULL,
    item_index INT,
    item_url VARCHAR(500),
    image_url VARCHAR(500),
    FOREIGN KEY (version_id) REFERENCES resource_template_version(version_id) ON DELETE CASCADE,
    INDEX idx_version_id (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Tạo index cho queries thường dùng
CREATE INDEX idx_version_template_status ON resource_template_version(template_id, status);
CREATE INDEX idx_version_created_by_status ON resource_template_version(created_by, status);
