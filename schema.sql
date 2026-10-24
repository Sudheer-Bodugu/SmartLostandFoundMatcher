CREATE DATABASE IF NOT EXISTS lost_found_db;

USE lost_found_db;

CREATE TABLE IF NOT EXISTS items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    item_type VARCHAR(10) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL
);
