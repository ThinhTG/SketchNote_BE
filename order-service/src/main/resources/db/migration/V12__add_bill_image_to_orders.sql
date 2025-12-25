-- Add bill_image column to orders table

ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS bill_image VARCHAR(500);

-- Add comment
COMMENT ON COLUMN orders.bill_image IS 'URL of the bill/invoice image uploaded by user';
