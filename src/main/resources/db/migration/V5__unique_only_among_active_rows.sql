-- V5__unique_only_among_active_rows.sql
-- Soft-deleted rows kept holding their username / VAT / email / ID number, so a
-- deleted customer locked those values forever and nobody could re-register with
-- them. Replace the plain UNIQUE constraints with partial unique indexes that
-- only consider rows which are still active.

ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_username;
CREATE UNIQUE INDEX uk_users_username_active
    ON users (username)
    WHERE deleted = FALSE;

ALTER TABLE personal_information DROP CONSTRAINT IF EXISTS uk_personal_information_id_number;
CREATE UNIQUE INDEX uk_personal_information_id_number_active
    ON personal_information (id_number)
    WHERE deleted = FALSE;

ALTER TABLE customers DROP CONSTRAINT IF EXISTS uk_customers_vat;
CREATE UNIQUE INDEX uk_customers_vat_active
    ON customers (vat)
    WHERE deleted = FALSE;

ALTER TABLE customers DROP CONSTRAINT IF EXISTS uk_customers_email;
CREATE UNIQUE INDEX uk_customers_email_active
    ON customers (email)
    WHERE deleted = FALSE;
