-- Drop existing tables if needed (be careful with this in production)
-- DROP TABLE IF EXISTS employees;

-- Create employees table if not exists
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    department VARCHAR(50) NOT NULL,
    position VARCHAR(50) NOT NULL,
    salary DECIMAL(10, 2),
    hire_date DATETIME NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    address VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- Insert sample data (only if table is empty)
INSERT IGNORE INTO employees (first_name, last_name, email, phone_number, department, position, salary, hire_date, active, address, city, country, created_at, updated_at) 
VALUES 
('John', 'Doe', 'john.doe@example.com', '+1234567890', 'IT', 'Senior Developer', 85000.00, NOW(), true, '123 Main St', 'New York', 'USA', NOW(), NOW()),
('Jane', 'Smith', 'jane.smith@example.com', '+1234567891', 'HR', 'HR Manager', 75000.00, NOW(), true, '456 Oak Ave', 'Los Angeles', 'USA', NOW(), NOW()),
('Bob', 'Johnson', 'bob.johnson@example.com', '+1234567892', 'Sales', 'Sales Executive', 65000.00, NOW(), true, '789 Pine Rd', 'Chicago', 'USA', NOW(), NOW()),
('Alice', 'Brown', 'alice.brown@example.com', '+1234567893', 'Marketing', 'Marketing Specialist', 70000.00, NOW(), true, '321 Elm St', 'Houston', 'USA', NOW(), NOW()),
('Charlie', 'Wilson', 'charlie.wilson@example.com', '+1234567894', 'IT', 'Junior Developer', 55000.00, NOW(), true, '654 Maple Dr', 'Phoenix', 'USA', NOW(), NOW());