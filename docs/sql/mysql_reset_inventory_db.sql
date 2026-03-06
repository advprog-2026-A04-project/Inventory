-- Clean setup for early-stage development (no legacy migration)
-- Run this in MySQL Workbench.

DROP DATABASE IF EXISTS inventory_db;
CREATE DATABASE inventory_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE inventory_db;

-- App user (recommended over root)
DROP USER IF EXISTS 'inventory_app'@'localhost';
CREATE USER 'inventory_app'@'localhost' IDENTIFIED BY 'inventory_pass';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_app'@'localhost';
FLUSH PRIVILEGES;

-- Schema aligned with Product entity
CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    stock INT NOT NULL,
    origin_location VARCHAR(255) NOT NULL,
    purchase_date DATE NOT NULL,
    jastiper_id VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_products_name (name),
    INDEX idx_products_jastiper (jastiper_id)
);
