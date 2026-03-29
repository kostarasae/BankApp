-- V1__initial_schema.sql
-- MySQL 8 / InnoDB / utf8mb4_0900_ai_ci

-- =========================
-- Security / Auth tables
-- =========================
CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name),

    INDEX idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE capabilities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT pk_capabilities PRIMARY KEY (id),
    CONSTRAINT uk_capabilities_name UNIQUE (name),

    INDEX idx_capabilities_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE roles_capabilities (
    role_id BIGINT NOT NULL,
    capability_id BIGINT NOT NULL,
    CONSTRAINT pk_roles_capabilities PRIMARY KEY (role_id, capability_id),

    CONSTRAINT fk_roles_capabilities_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_roles_capabilities_capability
        FOREIGN KEY (capability_id) REFERENCES capabilities(id)
        ON DELETE CASCADE,

    INDEX idx_roles_capabilities_capability_id (capability_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,

    uuid BINARY(16) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_users_uuid UNIQUE (uuid),
    CONSTRAINT uk_users_username UNIQUE (username),

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE RESTRICT,

    INDEX ix_users_role_id (role_id),
    INDEX ix_users_deleted (deleted),
    INDEX ix_users_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- =========================
-- Domain tables
-- =========================
CREATE TABLE regions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,

    CONSTRAINT pk_regions PRIMARY KEY (id),
    CONSTRAINT uk_regions_name UNIQUE (name),

    INDEX idx_regions_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE attachments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    filename VARCHAR(255) NULL,

    saved_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1024) NOT NULL,

    content_type VARCHAR(255) NULL,
    extension VARCHAR(50) NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,

    CONSTRAINT pk_attachments PRIMARY KEY (id),
    CONSTRAINT uk_attachments_saved_name UNIQUE (saved_name),

    INDEX idx_attachments_deleted (deleted),
    INDEX idx_attachments_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE personal_information (
    id BIGINT NOT NULL AUTO_INCREMENT,
    afm VARCHAR(255) NOT NULL,
    identity_number VARCHAR(255) NOT NULL,
    place_of_birth VARCHAR(255) NOT NULL,
    municipality_of_registration VARCHAR(255) NOT NULL,
    afm_file_id BIGINT,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,

    CONSTRAINT pk_personal_information PRIMARY KEY (id),
    CONSTRAINT uk_personal_information_afm UNIQUE (afm),
    CONSTRAINT uk_personal_information_identity UNIQUE (identity_number),
    CONSTRAINT uk_personal_information_afm_file UNIQUE (afm_file_id),

    CONSTRAINT fk_personal_information_attachments FOREIGN KEY (afm_file_id)
        REFERENCES attachments(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_personal_information_deleted (deleted),
    INDEX idx_personal_information_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(255) NOT NULL,
    iban VARCHAR(255),
    currency VARCHAR(10),
    balance DECIMAL(19,2),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,

    account_type VARCHAR(50) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (account_number),
    UNIQUE (iban)
);

CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid BINARY(16) NOT NULL,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    vat VARCHAR(255) NOT NULL,
    region_id BIGINT,
    account_id BIGINT,
    user_id BIGINT NOT NULL,
    personal_info_id BIGINT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uk_customers_uuid UNIQUE (uuid),
    CONSTRAINT uk_customers_vat UNIQUE (vat),

    -- enforce 1-1
    CONSTRAINT uk_customers_user_id UNIQUE (user_id),
    CONSTRAINT uk_customers_personal_info_id UNIQUE (personal_info_id),

    CONSTRAINT fk_customers_regions FOREIGN KEY (region_id)
        REFERENCES regions(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

     -- adjust users table name/PK if needed
    CONSTRAINT fk_customers_users FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_customers_personal_information FOREIGN KEY (personal_info_id)
        REFERENCES personal_information(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_customers_accounts FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_customers_region_id (region_id),
    INDEX idx_customers_lastname (lastname),
    INDEX idx_customers_deleted (deleted),
    INDEX idx_customers_deleted_at (deleted_at),
    INDEX idx_customers_user_id (user_id),
    INDEX idx_customers_personal_info_id (personal_info_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;