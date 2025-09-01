# 📚 Library Management System

## Giới thiệu

Hệ thống Quản lý Thư viện là một ứng dụng web được xây dựng bằng Spring Boot, cung cấp giải pháp toàn diện để quản lý hoạt động của hệ thống thư viện đa cấp (tỉnh/thành phố). Hệ thống hỗ trợ quản lý sách, người dùng, yêu cầu mượn sách, và các giao dịch mượn/trả sách một cách hiệu quả.

## 🎯 Tính năng chính

### 👥 Quản lý người dùng
- **Phân quyền đa cấp**: Admin, Quản lý tỉnh, Thủ thư, Độc giả
- **Xác thực và phân quyền**: JWT Authentication & Authorization
- **Quản lý hồ sơ người dùng**: Thông tin cá nhân, địa chỉ theo cấp hành chính

### 📖 Quản lý sách
- **Quản lý thông tin sách**: Tiêu đề, tác giả, nhà xuất bản, mô tả
- **Quản lý từng cuốn sách**: Mã sách, vị trí kệ, tình trạng, trạng thái
- **Theo dõi tình trạng sách**: Có sẵn, Đã mượn, Đặt trước, Mất, Bảo trì

### 🏛️ Quản lý thư viện
- **Hệ thống đa thư viện**: Quản lý nhiều thư viện theo khu vực
- **Phân cấp địa lý**: Tỉnh/thành phố - Xã/phường/thị trấn
- **Thông tin thư viện**: Địa chỉ, liên hệ, trạng thái hoạt động

### 📋 Quản lý mượn/trả sách
- **Yêu cầu mượn sách**: Tạo, phê duyệt, từ chối yêu cầu
- **Giao dịch mượn/trả**: Theo dõi ngày mượn, hạn trả, trả thực tế
- **Cảnh báo quá hạn**: Thông báo tự động cho sách quá hạn
- **Lịch sử di chuyển sách**: Theo dõi toàn bộ lịch sử của từng cuốn sách

### 🔔 Hệ thống thông báo
- **Thông báo tự động**: Phê duyệt yêu cầu, nhắc nhở trả sách, quá hạn
- **Quản lý thông báo**: Đánh dấu đã đọc, lọc theo loại thông báo

## 🛠️ Công nghệ sử dụng

### Backend
- **Java 17** - Ngôn ngữ lập trình chính
- **Spring Boot 3.5.5** - Framework chính
  - Spring Boot Starter Web - RESTful API
  - Spring Boot Starter Data JPA - ORM và database access
  - Spring Boot Starter Security - Bảo mật và xác thực
  - Spring Boot Starter Validation - Validation dữ liệu
- **JWT (JSON Web Token)** - Xác thực và phân quyền
- **Maven** - Quản lý dependencies và build tool

### Database
- **PostgreSQL 16.2** - Cơ sở dữ liệu chính
- **Hibernate** - ORM framework
- **Spring Data JPA** - Data access layer

### DevOps & Tools
- **Docker & Docker Compose** - Containerization
- **PgAdmin 4** - Database management tool
- **Spring Boot DevTools** - Development tools

## 📊 Database Schema

Hệ thống sử dụng cơ sở dữ liệu PostgreSQL với các bảng chính:

- **provinces** - Quản lý tỉnh/thành phố
- **communes** - Quản lý xã/phường/thị trấn  
- **libraries** - Thông tin các thư viện
- **accounts** - Tài khoản người dùng với phân quyền
- **book_titles** - Thông tin sách (metadata)
- **book_items** - Từng cuốn sách cụ thể
- **borrow_requests** - Yêu cầu mượn sách
- **borrowing_transactions** - Giao dịch mượn/trả
- **book_movements** - Lịch sử di chuyển sách
- **notifications** - Hệ thống thông báo
- **system_settings** - Cấu hình hệ thống

**Database Diagram**: [Xem chi tiết tại đây](https://dbdiagram.io/d/Library-67f3cf534f7afba184a17589)

## 🚀 Cài đặt và Chạy ứng dụng

### Yêu cầu hệ thống
- Java 17 hoặc cao hơn
- Maven 3.6+
- Docker & Docker Compose (khuyến nghị)
- PostgreSQL 16+ (nếu không dùng Docker)

### Cách 1: Chạy với Docker (Khuyến nghị)

1. **Clone repository**
```bash
git clone <repository-url>
cd library-management
```

2. **Tạo file .env từ template**
```bash
cp env.example .env
```

Sau đó chỉnh sửa file `.env` với các giá trị phù hợp:
```env
# Database Configuration
POSTGRES_DB=library_management
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password_here
POSTGRES_PORT=5433

# PgAdmin Configuration
PGADMIN_EMAIL=admin@library.com
PGADMIN_PASSWORD=your_secure_password_here

# Application Configuration
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080

# JWT Configuration
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here
JWT_EXPIRATION=86400000
```

3. **Chạy database với Docker Compose**
```bash
docker-compose up -d
```

4. **Chạy ứng dụng**
```bash
./mvnw spring-boot:run
```

### Cách 2: Chạy manual

1. **Cài đặt PostgreSQL và tạo database**
```sql
CREATE DATABASE library_management;
```

2. **Chạy script khởi tạo database**
```bash
psql -U postgres -d library_management -f sql/db.sql
psql -U postgres -d library_management -f sql/sample_data.sql
```

3. **Cập nhật cấu hình trong application.properties**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/library_management
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. **Chạy ứng dụng**
```bash
./mvnw spring-boot:run
```

## 📝 API Documentation

Sau khi chạy ứng dụng, bạn có thể truy cập:

- **Application**: http://localhost:8080
- **PgAdmin**: http://localhost:5050 (admin@library.com / admin123)

### Các endpoint chính:

#### Authentication
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/register` - Đăng ký

#### Books Management
- `GET /api/book-titles` - Lấy danh sách sách
- `POST /api/book-titles` - Thêm sách mới
- `GET /api/book-items` - Lấy danh sách cuốn sách
- `POST /api/book-items` - Thêm cuốn sách

#### Borrowing Management
- `POST /api/borrow-requests` - Tạo yêu cầu mượn sách
- `GET /api/borrow-requests` - Lấy danh sách yêu cầu
- `PUT /api/borrow-requests/{id}/review` - Phê duyệt/từ chối yêu cầu

#### Transactions
- `GET /api/borrowing-transactions` - Lấy danh sách giao dịch
- `POST /api/borrowing-transactions/{id}/return` - Trả sách

## 🔐 Phân quyền hệ thống

### ADMIN
- Quản lý toàn bộ hệ thống
- Quản lý tài khoản, thư viện, cấu hình hệ thống

### PROVINCE_MANAGER
- Quản lý các thư viện trong tỉnh
- Quản lý thủ thư và độc giả trong khu vực

### LIBRARIAN
- Quản lý sách trong thư viện
- Xử lý yêu cầu mượn/trả sách
- Quản lý giao dịch

### READER
- Xem danh sách sách
- Tạo yêu cầu mượn sách
- Xem lịch sử mượn sách

## 📁 Cấu trúc dự án

```
src/
├── main/
│   ├── java/com/library/
│   │   ├── config/          # Cấu hình Spring Security
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA Entities
│   │   ├── repository/     # Data Access Layer
│   │   ├── service/        # Business Logic Layer
│   │   ├── security/       # JWT Authentication
│   │   ├── scheduler/      # Scheduled Tasks
│   │   └── util/          # Utility Classes
│   └── resources/
│       ├── application.properties
│       └── static/
└── test/                   # Unit Tests
```


---

