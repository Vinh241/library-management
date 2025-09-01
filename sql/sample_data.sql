-- =====================================================
-- 02_insert_sample_data.sql
-- Chèn dữ liệu mẫu cho hệ thống quản lý thư viện
-- =====================================================
-- Usage: psql -U postgres -d library_management -f 02_insert_sample_data.sql
-- Chạy sau khi đã chạy 01_init_database_structure.sql

-- =====================================================
-- CHÈN DỮ LIỆU ĐỊA LÝ
-- =====================================================

-- Tỉnh/thành phố
INSERT INTO provinces (name, code) VALUES
('Hà Nội', 'HN'),
('Hồ Chí Minh', 'HCM'),
('Đà Nẵng', 'DN'),
('Hải Phòng', 'HP'),
('Cần Thơ', 'CT'),
('Nghệ An', 'NA'),
('Thanh Hóa', 'TH'),
('Bình Dương', 'BD');

-- Xã/phường
INSERT INTO communes (name, code, province_id) VALUES
-- Hà Nội
('Phường Hoàn Kiếm', 'HK', 1),
('Phường Đống Đa', 'DD', 1),
('Xã Đông Anh', 'DA', 1),
('Phường Cầu Giấy', 'CG', 1),
-- TP.HCM
('Phường Bến Nghé', 'BN', 2),
('Phường Đa Kao', 'DK', 2),
('Phường Tân Định', 'TD', 2),
-- Đà Nẵng
('Phường Thạch Thang', 'TT', 3),
('Phường Hải Châu', 'HC', 3),
-- Các tỉnh khác
('Phường Lê Chân', 'LC', 4),
('Phường An Hòa', 'AH', 5),
('Phường Vinh', 'V', 6),
('Phường Ba Đình', 'BTH', 7),
('Phường Hiệp Thành', 'HT', 8);

-- Thư viện
INSERT INTO libraries (name, address, phone, email, province_id, commune_id) VALUES
('Thư viện Hoàn Kiếm', '37 Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội', '024-3825-2614', 'lib.hoankiem@hanoi.gov.vn', 1, 1),
('Thư viện Đống Đa', '5 Phạm Ngọc Thạch, Đống Đa, Hà Nội', '024-3512-3456', 'lib.dongda@hanoi.gov.vn', 1, 2),
('Thư viện Cầu Giấy', '319 Cầu Giấy, Cầu Giấy, Hà Nội', '024-3755-2468', 'lib.caugiay@hanoi.gov.vn', 1, 4),
('Thư viện Thành phố HCM', '69 Lý Tự Trọng, Quận 1, TP.HCM', '028-3822-4062', 'info@thuvientphcm.gov.vn', 2, 5),
('Thư viện Quận 1', '01 Nguyễn Trãi, Quận 1, TP.HCM', '028-3925-1357', 'lib.q1@hcm.gov.vn', 2, 6),
('Thư viện Đà Nẵng', '25 Lê Duẩn, Hải Châu, Đà Nẵng', '0236-3821-344', 'library@danang.gov.vn', 3, 9),
('Thư viện Hải Phòng', '12 Điện Biên Phủ, Lê Chân, Hải Phòng', '0225-3822-156', 'lib@haiphong.gov.vn', 4, 10),
('Thư viện Cần Thơ', '02 Hòa Bình, Ninh Kiều, Cần Thơ', '0292-3830-474', 'thuvien@cantho.gov.vn', 5, 11);

-- =====================================================
-- CHÈN DỮ LIỆU TÀI KHOẢN
-- =====================================================
-- Mật khẩu mặc định: "password123" (đã hash bằng bcrypt)

-- Admin
INSERT INTO accounts (username, password_hash, full_name, email, phone, role) VALUES
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Nguyễn Văn Admin', 'admin@library.gov.vn', '0901234567', 'ADMIN'),
('superadmin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Trần Thị Super', 'super@library.gov.vn', '0901234568', 'ADMIN');

-- Province Managers
INSERT INTO accounts (username, password_hash, full_name, email, phone, role, province_id, commune_id) VALUES
('hanoi_manager', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Trần Thị Lan Hương', 'manager.hanoi@library.gov.vn', '0912345678', 'PROVINCE_MANAGER', 1, 1),
('hcm_manager', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Lê Văn Minh Quân', 'manager.hcm@library.gov.vn', '0923456789', 'PROVINCE_MANAGER', 2, 5),
('danang_manager', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Võ Thị Bích Ngọc', 'manager.dn@library.gov.vn', '0934567890', 'PROVINCE_MANAGER', 3, 8);

-- Librarians
INSERT INTO accounts (username, password_hash, full_name, email, phone, role, library_id, province_id, commune_id) VALUES
('librarian_hk', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Phạm Thị Thu Hương', 'thu.pham@hanoi.gov.vn', '0945678901', 'LIBRARIAN', 1, 1, 1),
('librarian_dd', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Võ Minh Đức Anh', 'duc.vo@hanoi.gov.vn', '0956789012', 'LIBRARIAN', 2, 1, 2),
('librarian_cg', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Đặng Văn Hải', 'hai.dang@hanoi.gov.vn', '0967890123', 'LIBRARIAN', 3, 1, 4),
('librarian_hcm1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Nguyễn Thị Mai Linh', 'mai.nguyen@hcm.gov.vn', '0978901234', 'LIBRARIAN', 4, 2, 5),
('librarian_hcm2', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Lê Hoàng Nam', 'nam.le@hcm.gov.vn', '0989012345', 'LIBRARIAN', 5, 2, 6),
('librarian_dn', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Trương Thị Yến', 'yen.truong@danang.gov.vn', '0990123456', 'LIBRARIAN', 6, 3, 9);

-- Readers (Độc giả)
INSERT INTO accounts (username, password_hash, full_name, email, phone, role, commune_id) VALUES
('reader01', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Hoàng Văn Nam', 'nam.hoang@email.com', '0967890123', 'READER', 1),
('reader02', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Bùi Thị Hoa Mai', 'hoa.bui@email.com', '0978901234', 'READER', 2),
('reader03', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Trần Minh Quân', 'quan.tran@email.com', '0989012345', 'READER', 5),
('reader04', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Lý Thị Nga', 'nga.ly@email.com', '0990123456', 'READER', 6),
('reader05', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Phan Văn Hùng', 'hung.phan@email.com', '0912345670', 'READER', 9),
('reader06', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Đinh Thị Lan', 'lan.dinh@email.com', '0923456781', 'READER', 1),
('reader07', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Vũ Minh Tâm', 'tam.vu@email.com', '0934567892', 'READER', 8),
('reader08', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeVMYaUC3JNjVTvL6', 'Lại Thị Thu', 'thu.lai@email.com', '0945678903', 'READER', 4);

-- =====================================================
-- CHÈN DỮ LIỆU SÁCH
-- =====================================================

-- Thông tin sách
INSERT INTO book_titles (isbn, title, author, publisher, published_year, category, description, language, total_pages) VALUES
('9786041141414', 'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', 'Nguyễn Nhật Ánh', 'NXB Trẻ', 2010, 'Văn học thiếu nhi', 'Tiểu thuyết nổi tiếng về tuổi thơ ở miền quê Việt Nam.', 'vi', 368),
('9786041042100', 'Dế Mèn Phiêu Lưu Ký', 'Tô Hoài', 'NXB Kim Đồng', 2015, 'Văn học thiếu nhi', 'Tác phẩm kinh điển của văn học Việt Nam dành cho trẻ em.', 'vi', 156),
('9786041018789', 'Số Đỏ', 'Vũ Trọng Phụng', 'NXB Văn học', 2018, 'Văn học hiện đại', 'Tiểu thuyết hiện thực phê phán nổi tiếng.', 'vi', 284),
('9786041125790', 'Chí Phèo', 'Nam Cao', 'NXB Văn học', 2017, 'Văn học hiện đại', 'Truyện ngắn nổi tiếng của Nam Cao.', 'vi', 98),
('9786041089567', 'Tuổi 20 Yêu Dấu', 'Nguyễn Huy Thiệp', 'NXB Trẻ', 2020, 'Văn học đương đại', 'Những câu chuyện về tuổi trẻ và tình yêu.', 'vi', 198),

-- Sách tiếng Anh
('9780061120084', 'To Kill a Mockingbird', 'Harper Lee', 'Harper Perennial', 1960, 'Fiction', 'A classic American novel about racial injustice.', 'en', 281),
('9780451524935', '1984', 'George Orwell', 'Signet Classics', 1949, 'Science Fiction', 'Dystopian novel about totalitarian society.', 'en', 328),
('9780316769488', 'The Catcher in the Rye', 'J.D. Salinger', 'Little Brown', 1951, 'Fiction', 'Coming-of-age story of Holden Caulfield.', 'en', 277),
('9780545010221', 'Harry Potter and the Sorcerers Stone', 'J.K. Rowling', 'Scholastic', 1997, 'Fantasy', 'First book in the Harry Potter series.', 'en', 309),

-- Sách khoa học và kỹ thuật
('9786041135468', 'Lập Trình Python Cơ Bản', 'Lê Văn Minh', 'NXB Thông tin và Truyền thông', 2021, 'Công nghệ thông tin', 'Giáo trình lập trình Python cho người mới bắt đầu.', 'vi', 325),
('9786041178234', 'Toán Cao Cấp A1', 'Đỗ Công Khanh', 'NXB Đại học Quốc gia', 2019, 'Toán học', 'Sách giáo khoa toán cao cấp dành cho sinh viên.', 'vi', 420),
('9786041234567', 'Vật Lý Đại Cương', 'Nguyễn Thành Long', 'NXB Giáo dục', 2020, 'Vật lý', 'Sách giáo khoa vật lý đại cương.', 'vi', 385);

-- Từng cuốn sách cụ thể (book_items)
INSERT INTO book_items (book_title_id, library_id, item_code, shelf_location, condition, status, acquisition_date, acquisition_cost) VALUES
-- Thư viện Hoàn Kiếm (library_id = 1)
(1, 1, 'HK-001-001', 'A1-01-01', 'EXCELLENT', 'AVAILABLE', '2023-01-15', 85000),
(1, 1, 'HK-001-002', 'A1-01-02', 'GOOD', 'AVAILABLE', '2023-01-15', 85000),
(1, 1, 'HK-001-003', 'A1-01-03', 'GOOD', 'AVAILABLE', '2023-06-10', 85000),
(2, 1, 'HK-002-001', 'A1-02-01', 'GOOD', 'AVAILABLE', '2023-02-10', 65000),
(2, 1, 'HK-002-002', 'A1-02-02', 'EXCELLENT', 'AVAILABLE', '2023-08-15', 65000),
(3, 1, 'HK-003-001', 'A2-01-01', 'EXCELLENT', 'AVAILABLE', '2023-03-05', 120000),
(4, 1, 'HK-004-001', 'A2-01-02', 'GOOD', 'AVAILABLE', '2023-04-12', 95000),
(6, 1, 'HK-006-001', 'B1-01-01', 'GOOD', 'AVAILABLE', '2023-01-20', 150000),
(7, 1, 'HK-007-001', 'B1-01-02', 'EXCELLENT', 'AVAILABLE', '2023-01-25', 180000),
(10, 1, 'HK-010-001', 'C1-01-01', 'GOOD', 'AVAILABLE', '2023-09-20', 220000),

-- Thư viện Đống Đa (library_id = 2)
(1, 2, 'DD-001-001', 'A1-01-01', 'GOOD', 'AVAILABLE', '2023-01-18', 85000),
(1, 2, 'DD-001-002', 'A1-01-02', 'EXCELLENT', 'AVAILABLE', '2023-07-20', 85000),
(2, 2, 'DD-002-001', 'A1-02-01', 'EXCELLENT', 'AVAILABLE', '2023-02-12', 65000),
(3, 2, 'DD-003-001', 'A2-01-01', 'GOOD', 'AVAILABLE', '2023-03-08', 120000),
(5, 2, 'DD-005-001', 'A3-01-01', 'GOOD', 'AVAILABLE', '2023-05-15', 89000),
(8, 2, 'DD-008-001', 'B1-03-01', 'GOOD', 'AVAILABLE', '2023-03-10', 160000),
(11, 2, 'DD-011-001', 'C1-01-01', 'EXCELLENT', 'AVAILABLE', '2023-10-05', 285000),

-- Thư viện TP.HCM (library_id = 4)
(1, 4, 'HCM-001-001', 'A1-01-01', 'EXCELLENT', 'AVAILABLE', '2023-01-20', 85000),
(1, 4, 'HCM-001-002', 'A1-01-02', 'GOOD', 'AVAILABLE', '2023-06-15', 85000),
(3, 4, 'HCM-003-001', 'A2-01-01', 'GOOD', 'AVAILABLE', '2023-03-08', 120000),
(4, 4, 'HCM-004-001', 'A2-02-01', 'EXCELLENT', 'AVAILABLE', '2023-04-15', 95000),
(6, 4, 'HCM-006-001', 'B1-01-01', 'EXCELLENT', 'AVAILABLE', '2023-01-22', 150000),
(9, 4, 'HCM-009-001', 'B2-01-01', 'GOOD', 'AVAILABLE', '2023-08-10', 175000),
(12, 4, 'HCM-012-001', 'C1-01-01', 'EXCELLENT', 'AVAILABLE', '2023-11-12', 295000),

-- Thư viện Đà Nẵng (library_id = 6)
(2, 6, 'DN-002-001', 'A1-01-01', 'GOOD', 'AVAILABLE', '2023-02-15', 65000),
(5, 6, 'DN-005-001', 'A2-01-01', 'EXCELLENT', 'AVAILABLE', '2023-05-20', 89000),
(7, 6, 'DN-007-001', 'B1-01-01', 'GOOD', 'AVAILABLE', '2023-03-25', 180000),
(8, 6, 'DN-008-001', 'B1-02-01', 'EXCELLENT', 'AVAILABLE', '2023-04-30', 160000);

-- =====================================================
-- CHÈN DỮ LIỆU YÊU CẦU VÀ GIAO DỊCH MƯỢN SÁCH
-- =====================================================

-- Yêu cầu mượn sách
INSERT INTO borrow_requests (book_item_id, requester_id, requested_borrow_date, requested_return_date, request_reason, status, reviewed_by, reviewed_at, review_notes) VALUES
-- Yêu cầu đã được duyệt
(1, 11, '2024-01-10', '2024-01-24', 'Nghiên cứu văn học Việt Nam cho luận văn', 'APPROVED', 6, '2024-01-09 14:30:00', 'Yêu cầu hợp lý, phục vụ nghiên cứu'),
(4, 12, '2024-01-12', '2024-01-26', 'Đọc để giải trí cuối tuần', 'APPROVED', 7, '2024-01-11 10:15:00', 'Chấp nhận yêu cầu'),
(15, 13, '2024-01-14', '2024-01-28', 'Tham khảo cho dự án học tập', 'APPROVED', 9, '2024-01-13 16:45:00', 'OK, sách có sẵn'),

-- Yêu cầu đang chờ duyệt
(7, 14, '2024-01-15', '2024-01-29', 'Học tiếng Anh cho kỳ thi TOEIC', 'PENDING', NULL, NULL, NULL),
(19, 15, '2024-01-16', '2024-01-30', 'Tham khảo cho luận văn về văn học', 'PENDING', NULL, NULL, NULL),
(11, 16, '2024-01-17', '2024-01-31', 'Nghiên cứu toán học cao cấp', 'PENDING', NULL, NULL, NULL),

-- Yêu cầu bị từ chối
(2, 11, '2024-01-08', '2024-01-22', 'Đọc thêm sách thiếu nhi', 'REJECTED', 6, '2024-01-07 16:45:00', 'Bạn đã mượn quá số lượng cho phép'),
(20, 17, '2024-01-05', '2024-01-19', 'Mượn để photocopy', 'REJECTED', 10, '2024-01-04 11:20:00', 'Không được phép photocopy sách');

-- Giao dịch mượn sách thực tế
INSERT INTO borrowing_transactions (borrow_request_id, book_item_id, borrower_id, librarian_id, borrow_date, due_date, status, notes) VALUES
(1, 1, 11, 6, '2024-01-10', '2024-01-24', 'ACTIVE', 'Giao sách thành công'),
(2, 4, 12, 7, '2024-01-12', '2024-01-26', 'ACTIVE', 'Độc giả nhận sách tại thư viện'),
(3, 15, 13, 9, '2024-01-14', '2024-01-28', 'ACTIVE', 'Mượn sách tiếng Anh');

-- =====================================================
-- CHÈN DỮ LIỆU LỊCH SỬ DI CHUYỂN SÁCH
-- =====================================================

-- Lịch sử di chuyển sách
INSERT INTO book_movements (book_item_id, transaction_id, action, old_status, new_status, old_location, new_location, performed_by, performed_at, notes) VALUES
-- Đặt trước sách
(1, NULL, 'RESERVED', 'AVAILABLE', 'RESERVED', 'A1-01-01', 'A1-01-01', 6, '2024-01-09 14:35:00', 'Đặt trước theo yêu cầu được duyệt'),
(4, NULL, 'RESERVED', 'AVAILABLE', 'RESERVED', 'A2-01-02', 'A2-01-02', 7, '2024-01-11 10:20:00', 'Dành cho độc giả'),
(15, NULL, 'RESERVED', 'AVAILABLE', 'RESERVED', 'A1-01-01', 'A1-01-01', 9, '2024-01-13 16:50:00', 'Đặt trước sách tiếng Anh'),

-- Cho mượn sách
(1, 1, 'BORROWED', 'RESERVED', 'BORROWED', 'A1-01-01', NULL, 6, '2024-01-10 09:00:00', 'Giao sách cho độc giả'),
(4, 2, 'BORROWED', 'RESERVED', 'BORROWED', 'A2-01-02', NULL, 7, '2024-01-12 14:30:00', 'Độc giả nhận sách'),
(15, 3, 'BORROWED', 'RESERVED', 'BORROWED', 'A1-01-01', NULL, 9, '2024-01-14 10:15:00', 'Cho mượn sách tiếng Anh'),

-- Di chuyển sách (sắp xếp lại kệ)
(3, NULL, 'MOVED', 'AVAILABLE', 'AVAILABLE', 'A2-01-01', 'A2-03-05', 6, '2024-01-05 11:00:00', 'Sắp xếp lại kệ sách văn học'),
(17, NULL, 'MOVED', 'AVAILABLE', 'AVAILABLE', 'C1-01-01', 'C1-02-03', 7, '2024-01-03 15:30:00', 'Chuyển sách sang kệ mới');

-- =====================================================
-- CHÈN DỮ LIỆU THÔNG BÁO
-- =====================================================

-- Thông báo cho độc giả
INSERT INTO notifications (recipient_id, type, title, message, request_id, transaction_id, is_read, sent_at) VALUES
-- Thông báo yêu cầu được duyệt
(11, 'REQUEST_APPROVED', 'Yêu cầu mượn sách được chấp nhận', 'Yêu cầu mượn sách "Tôi Thấy Hoa Vàng Trên Cỏ Xanh" của bạn đã được chấp nhận. Vui lòng đến Thư viện Hoàn Kiếm để nhận sách trong vòng 3 ngày.', 1, 1, TRUE, '2024-01-09 14:35:00'),
(12, 'REQUEST_APPROVED', 'Yêu cầu mượn sách được chấp nhận', 'Yêu cầu mượn sách "Chí Phèo" của bạn đã được chấp nhận. Vui lòng đến Thư viện Đống Đa để nhận sách.', 2, 2, FALSE, '2024-01-11 10:25:00'),
(13, 'REQUEST_APPROVED', 'Yêu cầu mượn sách được chấp nhận', 'Yêu cầu mượn sách "To Kill a Mockingbird" đã được chấp nhận. Thời gian nhận sách: 8h-17h30 các ngày trong tuần.', 3, 3, FALSE, '2024-01-13 16:55:00'),

-- Thông báo yêu cầu bị từ chối
(11, 'REQUEST_REJECTED', 'Yêu cầu mượn sách bị từ chối', 'Yêu cầu mượn sách "Dế Mèn Phiêu Lưu Ký" của bạn đã bị từ chối. Lý do: Bạn đã mượn quá số lượng sách cho phép (tối đa 5 cuốn).', 7, NULL, TRUE, '2024-01-07 16:50:00'),
(17, 'REQUEST_REJECTED', 'Yêu cầu mượn sách bị từ chối', 'Yêu cầu mượn sách "Vật Lý Đại Cương" đã bị từ chối. Lý do: Không được phép mượn sách để photocopy. Bạn có thể đọc tại chỗ.', 8, NULL, FALSE, '2024-01-04 11:25:00'),

-- Thông báo nhắc nhở sắp đến hạn trả
(11, 'DUE_REMINDER', 'Nhắc nhở sắp đến hạn trả sách', 'Sách "Tôi Thấy Hoa Vàng Trên Cỏ Xanh" sẽ đến hạn trả vào ngày 24/01/2024 (còn 3 ngày). Vui lòng chuẩn bị trả sách đúng hạn.', 1, 1, FALSE, '2024-01-21 09:00:00'),
(12, 'DUE_REMINDER', 'Nhắc nhở sắp đến hạn trả sách', 'Sách "Chí Phèo" sẽ đến hạn trả vào ngày 26/01/2024. Thời gian trả sách: 8h-17h30 các ngày làm việc.', 2, 2, FALSE, '2024-01-23 09:00:00');

-- =====================================================
-- CHÈN CẤU HÌNH HỆ THỐNG
-- =====================================================

