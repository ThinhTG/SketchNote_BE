-- Migration: Add PACKAGE_PURCHASE to credit_transactions type constraint
-- Date: 2025-12-03
-- Description: Thêm PACKAGE_PURCHASE vào constraint type của bảng credit_transactions

-- 1. Xóa constraint cũ (nếu tồn tại)
ALTER TABLE credit_transactions DROP CONSTRAINT IF EXISTS credit_transactions_type_check;

-- 2. Thêm constraint mới với tất cả các loại giao dịch
ALTER TABLE credit_transactions ADD CONSTRAINT credit_transactions_type_check 
    CHECK (type IN ('PURCHASE', 'PACKAGE_PURCHASE', 'USAGE', 'REFUND', 'BONUS', 'INITIAL_BONUS'));

-- 3. Cập nhật comment
COMMENT ON COLUMN credit_transactions.type IS 'Type Transaction: PURCHASE, PACKAGE_PURCHASE, USAGE, REFUND, BONUS, INITIAL_BONUS';
