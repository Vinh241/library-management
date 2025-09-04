### Dữ liệu với mỗi đầu sách có 1 quyển sách
### Thời gian phản hồi API (ms)

| Chức năng | 50K dữ liệu | 1M dữ liệu | 10M dữ liệu |
|-----------|-------------|------------|-------------|
| **Xem danh sách sách (trang 1)** | 68ms | 280ms | 1,250ms |
| **Xem danh sách sách (trang 10)** | 45ms | 220ms | 420ms |
| **Tìm kiếm sách** | 124ms | 280ms | 350ms |
| **Tìm kiếm nâng cao** | 96ms | 220ms | 280ms |
| **Xem danh sách bản copy** | 45ms | 95ms | 180ms |
| **Tìm kiếm bản copy** | 89ms | 150ms | 250ms |
| **Xem yêu cầu mượn** | 12ms | 18ms | 25ms |
| **Xem yêu cầu chờ duyệt** | 9ms | 15ms | 15ms |

---

### Tốc độ xử lý (requests/giây)

| Chức năng | 50K dữ liệu | 1M dữ liệu | 10M dữ liệu |
|-----------|-------------|------------|-------------|
| **Xem danh sách sách** | 71 req/s | 45 req/s | 25 req/s |
| **Tìm kiếm sách** | 39 req/s | 28 req/s | 20 req/s |
| **Xem danh sách bản copy** | 129 req/s | 85 req/s | 45 req/s |
| **Tìm kiếm bản copy** | 39 req/s | 28 req/s | 18 req/s |
| **Xem yêu cầu mượn** | 46 req/s | 35 req/s | 25 req/s |
| **Xem yêu cầu chờ duyệt** | 52 req/s | 45 req/s | 35 req/s |



### Thời gian thực hiện truy vấn (ms)

| Loại truy vấn | 50K dữ liệu | 1M dữ liệu | 10M dữ liệu |
|---------------|-------------|------------|-------------|
| **Đếm tổng số sách** | 11ms | 25ms | 85ms |
| **Đếm tổng số bản copy** | 6ms | 15ms | 25ms |
| **Tìm kiếm theo tên sách** | 4ms | 6ms | 6ms |
| **Tìm kiếm theo tác giả** | 29ms | 45ms | 120ms |
| **Tìm kiếm theo thể loại** | 2ms | 3ms | 3ms |
| **Thống kê sách có bản copy** | 67ms | 120ms | 450ms |
| **Sách có sẵn trong thư viện** | 21ms | 35ms | 180ms |
| **Phân trang sách (trang 1)** | 0ms | 1ms | 1ms |
| **Phân trang sách (trang 100)** | 2ms | 4ms | 4ms |
| **Phân trang sách (trang 1000)** | 7ms | 15ms | 25ms |
| **Tìm kiếm theo ISBN** | 0ms | 1ms | 1ms |
| **Tìm kiếm theo mã bản copy** | 1ms | 0ms | 0ms |
| **Thống kê theo thể loại** | 13ms | 25ms | 95ms |
| **Thống kê theo năm xuất bản** | 9ms | 18ms | 85ms |
| **Thống kê theo trạng thái** | 8ms | 15ms | 45ms |

---

### Dữ liệu với 1 đầu sách có rất nhiều bản copy

#### Thời gian phản hồi API (ms)

| Chức năng | 50K bản copy/đầu sách | 1M bản copy/đầu sách |
|-----------|-----------------------|----------------------|
| **Xem chi tiết đầu sách** | 40ms | 85ms |
| **Liệt kê bản copy (Page 1)** | 55ms | 120ms |
| **Liệt kê bản copy (Page 100)** | 95ms | 260ms |
| **Tìm kiếm bản copy trong đầu sách** | 80ms | 210ms |
| **Đếm bản copy theo trạng thái** | 110ms | 320ms |

#### Tốc độ xử lý (requests/giây)

| Chức năng | 50K bản copy/đầu sách | 1M bản copy/đầu sách |
|-----------|-----------------------|----------------------|
| **Liệt kê bản copy (Page 1)** | 120 req/s | 75 req/s |
| **Liệt kê bản copy (Page 100)** | 85 req/s | 38 req/s |
| **Tìm kiếm bản copy trong đầu sách** | 95 req/s | 52 req/s |
| **Đếm bản copy theo trạng thái** | 70 req/s | 28 req/s |

#### Thời gian truy vấn cơ sở dữ liệu (ms)

| Truy vấn | 50K bản copy/đầu sách | 1M bản copy/đầu sách |
|----------|-----------------------|----------------------|
| **COUNT book_items WHERE book_title_id = ?** | 18ms | 95ms |
| **GROUP BY status (trong 1 đầu sách)** | 42ms | 210ms |
| **Pagination book_items (Page 1)** | 7ms | 18ms |
| **Pagination book_items (Page 100)** | 18ms | 65ms |
| **Search item_code LIKE ? (scoped by title)** | 6ms | 22ms |
