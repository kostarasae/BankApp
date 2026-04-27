-- V3__seed_data.sql
-- Seed: 1 admin user, 2 customers (maria/nikos), 3 accounts, 11 transactions
-- Passwords: Admin1234! / Test1234!

-- ============================================================
-- 1. Personal information
-- ============================================================
INSERT INTO personal_information
    (id_number, place_of_birth, municipality_of_registration,
     date_of_birth, home_address, gender,
     created_at, updated_at)
VALUES
    ('ΑΒ123456', 'Αθήνα',       'Αθήνα',       '1988-03-15', 'Ερμού 12, Αθήνα',          'FEMALE', NOW(), NOW()),
    ('ΓΔ789012', 'Θεσσαλονίκη', 'Θεσσαλονίκη', '1991-09-22', 'Τσιμισκή 5, Θεσσαλονίκη', 'MALE',   NOW(), NOW());

-- ============================================================
-- 2. Users
-- ============================================================
INSERT INTO users (uuid, username, password, role_id, created_at, updated_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'admin',
     '$2a$12$JLal3vKArBUaA.LZS.gbXOsQ/YUt5etjZIpcB0DA2UhHvARgxwhT.',
     (SELECT id FROM roles WHERE name = 'ADMIN'),
     NOW(), NOW()),

    ('11111111-1111-1111-1111-111111111111',
     'maria',
     '$2a$12$YaIMYLpFVJEW4ovPAQHYHOuQqWNhhl6ZsOkp9Tt9Hu4fU9P2X9I6C',
     (SELECT id FROM roles WHERE name = 'CUSTOMER'),
     NOW(), NOW()),

    ('22222222-2222-2222-2222-222222222222',
     'nikos',
     '$2a$12$YaIMYLpFVJEW4ovPAQHYHOuQqWNhhl6ZsOkp9Tt9Hu4fU9P2X9I6C',
     (SELECT id FROM roles WHERE name = 'CUSTOMER'),
     NOW(), NOW());

-- ============================================================
-- 3. Accounts
-- ============================================================
INSERT INTO accounts (account_number, iban, currency, balance, account_type, created_at, updated_at)
VALUES
    ('ACC-MARIA-CHK', 'GR1600000000000000000000001', 'EUR', 1350.00, 'CHECKING', NOW(), NOW()),
    ('ACC-MARIA-SAV', 'GR1600000000000000000000002', 'EUR',  700.00, 'SAVINGS',  NOW(), NOW()),
    ('ACC-NIKOS-CHK', 'GR1600000000000000000000003', 'EUR',  450.00, 'CHECKING', NOW(), NOW());

-- ============================================================
-- 4. Customers
-- ============================================================
INSERT INTO customers
    (uuid, firstname, lastname, vat, email, phone,
     region_id, user_id, personal_info_id,
     created_at, updated_at)
VALUES
    ('33333333-3333-3333-3333-333333333333',
     'Μαρία', 'Παπαδοπούλου', '123456789', 'maria@test.gr', '6900000001',
     (SELECT id FROM regions WHERE name = 'ΑΤΤΙΚΗΣ'),
     (SELECT id FROM users  WHERE username = 'maria'),
     (SELECT id FROM personal_information WHERE id_number = 'ΑΒ123456'),
     NOW(), NOW()),

    ('44444444-4444-4444-4444-444444444444',
     'Νίκος', 'Γεωργίου', '987654321', 'nikos@test.gr', '6900000002',
     (SELECT id FROM regions WHERE name = 'ΑΤΤΙΚΗΣ'),
     (SELECT id FROM users  WHERE username = 'nikos'),
     (SELECT id FROM personal_information WHERE id_number = 'ΓΔ789012'),
     NOW(), NOW());

-- ============================================================
-- 5. customers_accounts (join)
-- ============================================================
INSERT INTO customers_accounts (customer_id, account_id)
VALUES
    ((SELECT id FROM customers WHERE vat = '123456789'),
     (SELECT id FROM accounts  WHERE iban = 'GR1600000000000000000000001')),

    ((SELECT id FROM customers WHERE vat = '123456789'),
     (SELECT id FROM accounts  WHERE iban = 'GR1600000000000000000000002')),

    ((SELECT id FROM customers WHERE vat = '987654321'),
     (SELECT id FROM accounts  WHERE iban = 'GR1600000000000000000000003'));

-- ============================================================
-- 6. Transactions
-- ============================================================

-- Μαρία — τρεχούμενος (GR...001)
INSERT INTO transactions (iban, amount, type, description, created_at, updated_at)
VALUES
    ('GR1600000000000000000000001', 500.00, 'DEPOSIT',    'Κατάθεση ATM Σύνταγμα',   NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    ('GR1600000000000000000000001', 850.00, 'DEPOSIT',    'Μισθός Απριλίου',          NOW() - INTERVAL '1 day',  NOW() - INTERVAL '1 day'),
    ('GR1600000000000000000000001', 150.00, 'WITHDRAWAL', 'Ανάληψη ATM Ομόνοια',     NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    ('GR1600000000000000000000001', 300.00, 'WITHDRAWAL', 'Ανάληψη κατάστημα',       NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    ('GR1600000000000000000000001', 100.00, 'TRANSFER',   'IRIS μεταφορά προς Νίκο', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    ('GR1600000000000000000000001', 200.00, 'TRANSFER',   'ΔΕΗ πληρωμή',             NOW() - INTERVAL '1 day',  NOW() - INTERVAL '1 day');

-- Μαρία — ταμιευτήριο (GR...002)
INSERT INTO transactions (iban, amount, type, description, created_at, updated_at)
VALUES
    ('GR1600000000000000000000002', 500.00, 'DEPOSIT', 'Αρχικό ποσό',  NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    ('GR1600000000000000000000002', 200.00, 'DEPOSIT', 'Εξοικονόμηση', NOW() - INTERVAL '3 days',  NOW() - INTERVAL '3 days');

-- Νίκος — τρεχούμενος (GR...003)
INSERT INTO transactions (iban, amount, type, description, created_at, updated_at)
VALUES
    ('GR1600000000000000000000003', 300.00, 'DEPOSIT',    'Κατάθεση ATM Θεσσαλονίκη', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
    ('GR1600000000000000000000003', 100.00, 'DEPOSIT',    'IRIS μεταφορά από Μαρία',   NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    ('GR1600000000000000000000003', 150.00, 'WITHDRAWAL', 'Ανάληψη ATM Μοναστηράκι',  NOW() - INTERVAL '1 day',  NOW() - INTERVAL '1 day');
