-- Migration: Add AI Credits System
-- Date: 2025-12-02
-- Description: Thêm hệ thống credit cho AI features

-- 1. Thêm column ai_credits vào bảng users
ALTER TABLE users ADD COLUMN IF NOT EXISTS ai_credits INTEGER NOT NULL DEFAULT 0;

-- 2. Tạo bảng credit_transactions để lưu lịch sử giao dịch credit
CREATE TABLE IF NOT EXISTS credit_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    description VARCHAR(255),
    reference_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Tạo index để tăng performance khi query
CREATE INDEX IF NOT EXISTS idx_credit_transactions_user_id ON credit_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_credit_transactions_type ON credit_transactions(type);
CREATE INDEX IF NOT EXISTS idx_credit_transactions_created_at ON credit_transactions(created_at DESC);

-- 4. Comment cho các bảng và columns
COMMENT ON COLUMN users.ai_credits IS 'Số credit AI còn lại của user';
COMMENT ON TABLE credit_transactions IS 'Lịch sử giao dịch credit của users';
COMMENT ON COLUMN credit_transactions.type IS 'Loại giao dịch: PURCHASE, USAGE, REFUND, BONUS, INITIAL_BONUS';
COMMENT ON COLUMN credit_transactions.amount IS 'Số lượng credit (dương: thêm, âm: trừ)';
COMMENT ON COLUMN credit_transactions.balance_after IS 'Số dư sau giao dịch';
COMMENT ON COLUMN credit_transactions.reference_id IS 'ID tham chiếu (orderId, imageId, etc.)';
