-- =====================================================
-- 01_init_database_structure.sql
-- Tạo cấu trúc database cho hệ thống quản lý thư viện
-- =====================================================
-- Usage: psql -U postgres -d library_management -f 01_init_database_structure.sql

-- Xóa bảng cũ nếu có (cẩn thận khi chạy production!)
DROP TABLE IF EXISTS system_settings CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS book_movements CASCADE;
DROP TABLE IF EXISTS borrowing_transactions CASCADE;
DROP TABLE IF EXISTS borrow_requests CASCADE;
DROP TABLE IF EXISTS book_items CASCADE;
DROP TABLE IF EXISTS book_titles CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS libraries CASCADE;
DROP TABLE IF EXISTS communes CASCADE;
DROP TABLE IF EXISTS provinces CASCADE;

-- =====================================================
-- TẠO CÁC BẢNG
-- =====================================================

-- Bảng tỉnh/thành phố
CREATE TABLE provinces (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng xã/phường/thị trấn
CREATE TABLE communes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    province_id INTEGER NOT NULL REFERENCES provinces(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(province_id, code)
);

-- Bảng thư viện
CREATE TABLE libraries (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    province_id INTEGER NOT NULL REFERENCES provinces(id),
    commune_id INTEGER REFERENCES communes(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng tài khoản người dùng
CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'PROVINCE_MANAGER', 'LIBRARIAN', 'READER')),
    library_id INTEGER REFERENCES libraries(id),
    province_id INTEGER REFERENCES provinces(id),
    commune_id INTEGER REFERENCES communes(id),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng thông tin sách
CREATE TABLE book_titles (
    id SERIAL PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(300) NOT NULL,
    publisher VARCHAR(200),
    published_year INTEGER,
    category VARCHAR(100),
    description TEXT,
    cover_image VARCHAR(500),
    language VARCHAR(10) DEFAULT 'vi',
    total_pages INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng từng cuốn sách cụ thể
CREATE TABLE book_items (
    id SERIAL PRIMARY KEY,
    book_title_id INTEGER NOT NULL REFERENCES book_titles(id),
    library_id INTEGER NOT NULL REFERENCES libraries(id),
    item_code VARCHAR(50) UNIQUE NOT NULL,
    shelf_location VARCHAR(100),
    condition VARCHAR(20) DEFAULT 'GOOD' CHECK (condition IN ('EXCELLENT', 'GOOD', 'FAIR', 'DAMAGED')),
    status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'BORROWED', 'RESERVED', 'LOST', 'MAINTENANCE')),
    acquisition_date DATE NOT NULL,
    acquisition_cost DECIMAL(10,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng yêu cầu mượn sách
CREATE TABLE borrow_requests (
    id SERIAL PRIMARY KEY,
    book_item_id INTEGER NOT NULL REFERENCES book_items(id),
    requester_id INTEGER NOT NULL REFERENCES accounts(id),
    requested_borrow_date DATE NOT NULL,
    requested_return_date DATE NOT NULL,
    request_reason TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')),
    reviewed_by INTEGER REFERENCES accounts(id),
    reviewed_at TIMESTAMP,
    review_notes TEXT,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng giao dịch mượn/trả sách
CREATE TABLE borrowing_transactions (
    id SERIAL PRIMARY KEY,
    borrow_request_id INTEGER NOT NULL UNIQUE REFERENCES borrow_requests(id),
    book_item_id INTEGER NOT NULL REFERENCES book_items(id),
    borrower_id INTEGER NOT NULL REFERENCES accounts(id),
    librarian_id INTEGER NOT NULL REFERENCES accounts(id),
    borrow_date DATE DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    return_date DATE,
    returned_to INTEGER REFERENCES accounts(id),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'RETURNED', 'OVERDUE', 'LOST')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng lịch sử di chuyển sách
CREATE TABLE book_movements (
    id SERIAL PRIMARY KEY,
    book_item_id INTEGER NOT NULL REFERENCES book_items(id),
    transaction_id INTEGER REFERENCES borrowing_transactions(id),
    action VARCHAR(20) NOT NULL CHECK (action IN ('RESERVED', 'BORROWED', 'RETURNED', 'MOVED', 'LOST', 'FOUND', 'DAMAGED')),
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    old_location VARCHAR(100),
    new_location VARCHAR(100),
    performed_by INTEGER NOT NULL REFERENCES accounts(id),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Bảng thông báo
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    recipient_id INTEGER NOT NULL REFERENCES accounts(id),
    type VARCHAR(30) NOT NULL CHECK (type IN ('REQUEST_APPROVED', 'REQUEST_REJECTED', 'DUE_REMINDER', 'OVERDUE_NOTICE')),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    request_id INTEGER REFERENCES borrow_requests(id),
    transaction_id INTEGER REFERENCES borrowing_transactions(id),
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Bảng cấu hình hệ thống
CREATE TABLE system_settings (
    id SERIAL PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    description TEXT,
    updated_by INTEGER REFERENCES accounts(id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TẠO INDEX CƠ BẢN (CHỈ NHỮNG GÌ CẦN THIẾT)
-- =====================================================

-- Index cho tìm kiếm sách
CREATE INDEX idx_book_titles_title ON book_titles(title);
CREATE INDEX idx_book_titles_author ON book_titles(author);

-- Index cho trạng thái sách
CREATE INDEX idx_book_items_status ON book_items(status);
CREATE INDEX idx_book_items_library_id ON book_items(library_id);

-- Index cho yêu cầu mượn sách
CREATE INDEX idx_borrow_requests_status ON borrow_requests(status);
CREATE INDEX idx_borrow_requests_requester_id ON borrow_requests(requester_id);

-- Index cho giao dịch mượn/trả
CREATE INDEX idx_borrowing_transactions_status ON borrowing_transactions(status);
CREATE INDEX idx_borrowing_transactions_borrower_id ON borrowing_transactions(borrower_id);
CREATE INDEX idx_borrowing_transactions_due_date ON borrowing_transactions(due_date);

-- Index cho thông báo
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

