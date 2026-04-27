-- V1__schema.sql
-- PostgreSQL — complete final schema

-- =========================
-- Security / Auth tables
-- =========================
CREATE TABLE roles (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
);
CREATE INDEX idx_roles_name ON roles (name);

CREATE TABLE capabilities (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT pk_capabilities PRIMARY KEY (id),
    CONSTRAINT uk_capabilities_name UNIQUE (name)
);
CREATE INDEX idx_capabilities_name ON capabilities (name);

CREATE TABLE roles_capabilities (
    role_id BIGINT NOT NULL,
    capability_id BIGINT NOT NULL,
    CONSTRAINT pk_roles_capabilities PRIMARY KEY (role_id, capability_id),
    CONSTRAINT fk_roles_capabilities_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_roles_capabilities_capability
        FOREIGN KEY (capability_id) REFERENCES capabilities(id) ON DELETE CASCADE
);
CREATE INDEX idx_roles_capabilities_capability_id ON roles_capabilities (capability_id);

CREATE TABLE users (
    id BIGSERIAL NOT NULL,
    uuid UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_uuid UNIQUE (uuid),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);
CREATE INDEX ix_users_role_id ON users (role_id);
CREATE INDEX ix_users_deleted ON users (deleted);
CREATE INDEX ix_users_deleted_at ON users (deleted_at);


-- =========================
-- Domain tables
-- =========================
CREATE TABLE regions (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_regions PRIMARY KEY (id),
    CONSTRAINT uk_regions_name UNIQUE (name)
);
CREATE INDEX idx_regions_name ON regions (name);

CREATE TABLE attachments (
    id BIGSERIAL NOT NULL,
    filename VARCHAR(255) NULL,
    saved_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    content_type VARCHAR(255) NULL,
    extension VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT pk_attachments PRIMARY KEY (id),
    CONSTRAINT uk_attachments_saved_name UNIQUE (saved_name)
);
CREATE INDEX idx_attachments_deleted ON attachments (deleted);
CREATE INDEX idx_attachments_deleted_at ON attachments (deleted_at);

CREATE TABLE personal_information (
    id BIGSERIAL NOT NULL,
    id_number VARCHAR(255) NOT NULL,
    place_of_birth VARCHAR(255) NOT NULL,
    municipality_of_registration VARCHAR(255) NOT NULL,
    date_of_birth VARCHAR(255) NOT NULL,
    home_address VARCHAR(255) NOT NULL,
    gender VARCHAR(255) NOT NULL,
    id_file_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT pk_personal_information PRIMARY KEY (id),
    CONSTRAINT uk_personal_information_id_number UNIQUE (id_number),
    CONSTRAINT uk_personal_information_id_file UNIQUE (id_file_id),
    CONSTRAINT fk_personal_information_id_file FOREIGN KEY (id_file_id)
        REFERENCES attachments(id) ON DELETE SET NULL
);
CREATE INDEX idx_personal_information_deleted ON personal_information (deleted);
CREATE INDEX idx_personal_information_deleted_at ON personal_information (deleted_at);

CREATE TABLE accounts (
    id BIGSERIAL NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    iban VARCHAR(255),
    currency VARCHAR(10),
    balance DECIMAL(19,2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    account_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (account_number),
    UNIQUE (iban)
);

CREATE TABLE customers (
    id BIGSERIAL NOT NULL,
    uuid UUID NOT NULL,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    vat VARCHAR(255) NOT NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(255) NOT NULL,
    region_id BIGINT,
    account_id BIGINT,
    user_id BIGINT NOT NULL,
    personal_info_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uk_customers_uuid UNIQUE (uuid),
    CONSTRAINT uk_customers_vat UNIQUE (vat),
    CONSTRAINT uk_customers_email UNIQUE (email),
    CONSTRAINT uk_customers_user_id UNIQUE (user_id),
    CONSTRAINT uk_customers_personal_info_id UNIQUE (personal_info_id),
    CONSTRAINT fk_customers_regions FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE SET NULL,
    CONSTRAINT fk_customers_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_customers_personal_information FOREIGN KEY (personal_info_id) REFERENCES personal_information(id) ON DELETE RESTRICT,
    CONSTRAINT fk_customers_accounts FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE SET NULL
);
CREATE INDEX idx_customers_region_id ON customers (region_id);
CREATE INDEX idx_customers_lastname ON customers (lastname);
CREATE INDEX idx_customers_deleted ON customers (deleted);
CREATE INDEX idx_customers_deleted_at ON customers (deleted_at);
CREATE INDEX idx_customers_user_id ON customers (user_id);
CREATE INDEX idx_customers_personal_info_id ON customers (personal_info_id);

CREATE TABLE customers_accounts (
    customer_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    CONSTRAINT pk_customers_accounts PRIMARY KEY (customer_id, account_id),
    CONSTRAINT fk_customers_accounts_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_customers_accounts_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);
CREATE INDEX idx_customers_accounts_customers_id ON customers_accounts (customer_id);

CREATE TABLE transactions (
    id          BIGSERIAL NOT NULL,
    iban        VARCHAR(27) NOT NULL,
    amount      DECIMAL(15,2) NOT NULL,
    type        VARCHAR(20) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    description VARCHAR(255) NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT fk_transactions_accounts FOREIGN KEY (iban)
        REFERENCES accounts(iban) ON DELETE RESTRICT
);
CREATE INDEX idx_transactions_iban ON transactions (iban);
