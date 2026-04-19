ALTER TABLE transactions
    ADD COLUMN created_at  DATETIME NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at  DATETIME NOT NULL DEFAULT NOW(),
    ADD COLUMN deleted     TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN deleted_at  DATETIME NULL,
    DROP COLUMN transaction_date,
    DROP COLUMN transaction_time;