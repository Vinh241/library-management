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

