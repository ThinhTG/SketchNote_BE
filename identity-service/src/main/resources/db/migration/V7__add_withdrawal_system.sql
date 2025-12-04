-- Create withdrawal_request table
CREATE TABLE IF NOT EXISTS withdrawal_request (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    bank_account_number VARCHAR(50) NOT NULL,
    bank_account_holder VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    staff_id BIGINT,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_withdrawal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_withdrawal_amount CHECK (amount > 0),
    CONSTRAINT chk_withdrawal_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_withdrawal_user ON withdrawal_request(user_id);
CREATE INDEX IF NOT EXISTS idx_withdrawal_status ON withdrawal_request(status);
CREATE INDEX IF NOT EXISTS idx_withdrawal_created ON withdrawal_request(created_at);

-- Add comment for documentation
COMMENT ON TABLE withdrawal_request IS 'Stores customer withdrawal requests to bank accounts';
COMMENT ON COLUMN withdrawal_request.user_id IS 'Reference to the user who made the withdrawal request';
COMMENT ON COLUMN withdrawal_request.amount IS 'Amount to be withdrawn in VND';
COMMENT ON COLUMN withdrawal_request.status IS 'Current status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN withdrawal_request.staff_id IS 'Staff member who processed the request (nullable)';
