-- V2__static_data.sql
-- Regions, roles, and all capabilities

-- =========================
-- Regions
-- =========================
INSERT INTO regions (id, name) VALUES
(1,  'ΑΝΑΤΟΛΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ ΚΑΙ ΘΡΑΚΗΣ'),
(2,  'ΑΤΤΙΚΗΣ'),
(3,  'ΒΟΡΕΙΟΥ ΑΙΓΑΙΟΥ'),
(4,  'ΔΥΤΙΚΗΣ ΕΛΛΑΔΑΣ'),
(5,  'ΔΥΤΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ'),
(6,  'ΗΠΕΙΡΟΥ'),
(7,  'ΘΕΣΣΑΛΙΑΣ'),
(8,  'ΙΟΝΙΩΝ ΝΗΣΩΝ'),
(9,  'ΚΕΝΤΡΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ'),
(10, 'ΚΡΗΤΗΣ'),
(11, 'ΝΟΤΙΟΥ ΑΙΓΑΙΟΥ'),
(12, 'ΠΕΛΟΠΟΝΝΗΣΟΥ'),
(13, 'ΣΤΕΡΕΑΣ ΕΛΛΑΔΑΣ');
SELECT setval(pg_get_serial_sequence('regions', 'id'), 13);

-- =========================
-- Roles
-- =========================
INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('EMPLOYEE'),
    ('CUSTOMER');

-- =========================
-- Capabilities
-- =========================
INSERT INTO capabilities (name, description) VALUES
    ('INSERT_CUSTOMER',    'Create a new customer'),
    ('VIEW_CUSTOMERS',     'View customer list and details'),
    ('VIEW_CUSTOMER',      'View customer'),
    ('EDIT_CUSTOMER',      'Modify existing customer'),
    ('DELETE_CUSTOMER',    'Remove a customer'),
    ('VIEW_ONLY_CUSTOMER', 'View only own customer details'),
    ('CREATE_ACCOUNT',     'Create a new account'),
    ('VIEW_ACCOUNTS',      'View account list and details'),
    ('VIEW_ACCOUNT',       'View account'),
    ('DELETE_ACCOUNT',     'Remove an account'),
    ('CAN_DEPOSIT',        'Deposit to account'),
    ('CAN_WITHDRAW',       'Withdraw from account'),
    ('CAN_TRANSFER',       'Transfer funds between accounts'),
    ('VIEW_USER',          'View user details'),
    ('VIEW_ONLY_ACCOUNT',  'View only own account details');

-- =========================
-- Role-capability assignments
-- =========================

-- ADMIN gets all capabilities
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r CROSS JOIN capabilities c
WHERE r.name = 'ADMIN';

-- EMPLOYEE capabilities
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r CROSS JOIN capabilities c
WHERE r.name = 'EMPLOYEE'
  AND c.name IN (
      'VIEW_CUSTOMERS', 'VIEW_CUSTOMER', 'INSERT_CUSTOMER', 'EDIT_CUSTOMER',
      'VIEW_ACCOUNTS', 'VIEW_ACCOUNT', 'CREATE_ACCOUNT'
  );

-- CUSTOMER capabilities
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r CROSS JOIN capabilities c
WHERE r.name = 'CUSTOMER'
  AND c.name IN ('VIEW_ONLY_CUSTOMER', 'CAN_DEPOSIT', 'CAN_WITHDRAW', 'CAN_TRANSFER', 'VIEW_ONLY_ACCOUNT');
