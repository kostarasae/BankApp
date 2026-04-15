-- Insert roles
INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('EMPLOYEE'),
    ('CUSTOMER');

-- Insert capabilities
INSERT INTO capabilities (name, description)
VALUES
    ('INSERT_CUSTOMER', 'Create a new customer'),
    ('VIEW_CUSTOMERS', 'View customer list and details'),
    ('VIEW_CUSTOMER', 'View customer'),
    ('EDIT_CUSTOMER', 'Modify existing customer'),
    ('DELETE_CUSTOMER', 'Remove a customer'),
    ('VIEW_ONLY_CUSTOMER', 'View only own customer details'),
    ('CREATE_ACCOUNT', 'Create a new account'),
    ('VIEW_ACCOUNTS', 'View account list and details'),
    ('VIEW_ACCOUNT', 'View account'),
    ('DELETE_ACCOUNT', 'Remove an account'),
    ('CAN_DEPOSIT', 'Deposit to account'),
    ('CAN_WITHDRAW', 'Withdraw from account');

-- Assign capabilities to ADMIN (all capabilities)
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
JOIN capabilities c
WHERE r.name = 'ADMIN';

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
JOIN capabilities c
WHERE r.name = 'EMPLOYEE'
  AND c.name IN ('VIEW_CUSTOMERS', 'VIEW_CUSTOMER', 'INSERT_CUSTOMER', 'EDIT_CUSTOMER', 'VIEW_ACCOUNTS', 'VIEW_ACCOUNT', 'CREATE_ACCOUNT');

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
JOIN capabilities c
WHERE r.name = 'CUSTOMER'
AND c.name IN ('VIEW_ONLY_CUSTOMER','CAN_DEPOSIT', 'CAN_WITHDRAW');
