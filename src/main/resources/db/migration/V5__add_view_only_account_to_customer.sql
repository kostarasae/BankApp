-- Add VIEW_ONLY_ACCOUNT capability and assign to CUSTOMER role
INSERT INTO capabilities (name, description)
VALUES ('VIEW_ONLY_ACCOUNT', 'View only own account details');

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
JOIN capabilities c
WHERE r.name = 'CUSTOMER'
  AND c.name = 'VIEW_ONLY_ACCOUNT';
