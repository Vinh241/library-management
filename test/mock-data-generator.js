/**
 * Mock Data Generator cho hệ thống quản lý thư viện
 * Tạo dữ liệu test cho 1M, 10M, 100M quyển sách
 */

const fs = require('fs');
const path = require('path');

class MockDataGenerator {
    constructor() {
        this.baseUrl = 'http://localhost:8080/api';
        this.authToken = null;

        // Dữ liệu mẫu
        this.sampleTitles = [
            'Lập trình Java từ cơ bản đến nâng cao',
            'Spring Boot trong thực tế',
            'Microservices Architecture',
            'Database Design và Optimization',
            'React.js Development Guide',
            'Node.js Backend Development',
            'Docker và Kubernetes',
            'Machine Learning với Python',
            'Data Science và Analytics',
            'Cloud Computing với AWS',
            'DevOps và CI/CD',
            'Security trong Web Development',
            'Mobile App Development',
            'Game Development với Unity',
            'Blockchain và Cryptocurrency',
            'Artificial Intelligence',
            'Internet of Things (IoT)',
            'Cybersecurity Fundamentals',
            'Software Engineering Principles',
            'Agile và Scrum Methodology'
        ];

        this.sampleAuthors = [
            'Nguyễn Văn A', 'Trần Thị B', 'Lê Văn C', 'Phạm Thị D', 'Hoàng Văn E',
            'Vũ Thị F', 'Đặng Văn G', 'Bùi Thị H', 'Phan Văn I', 'Ngô Thị K',
            'Dương Văn L', 'Lý Thị M', 'Đinh Văn N', 'Tôn Thị O', 'Võ Văn P',
            'Robert Martin', 'Martin Fowler', 'Kent Beck', 'Eric Evans', 'Uncle Bob'
        ];

        this.samplePublishers = [
            'NXB Giáo dục Việt Nam', 'NXB Khoa học và Kỹ thuật', 'NXB Thông tin và Truyền thông',
            'NXB Đại học Quốc gia', 'NXB Tổng hợp TP.HCM', 'NXB Hà Nội',
            'O\'Reilly Media', 'Packt Publishing', 'Manning Publications',
            'Addison-Wesley', 'Wiley', 'McGraw-Hill'
        ];

        this.sampleCategories = [
            'Lập trình', 'Cơ sở dữ liệu', 'Mạng máy tính', 'Trí tuệ nhân tạo',
            'Khoa học máy tính', 'Công nghệ thông tin', 'Phần mềm', 'Hệ thống',
            'Bảo mật', 'Web Development', 'Mobile Development', 'Game Development'
        ];

        this.sampleConditions = ['EXCELLENT', 'GOOD', 'FAIR', 'DAMAGED'];
        this.sampleStatuses = ['AVAILABLE', 'BORROWED', 'RESERVED', 'LOST', 'MAINTENANCE'];
        this.sampleShelfLocations = ['A1', 'A2', 'A3', 'B1', 'B2', 'B3', 'C1', 'C2', 'C3'];
    }

    /**
     * Đăng nhập để lấy token
     */
    async login() {
        try {
            const response = await fetch(`${this.baseUrl}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: 'admin', // Thay đổi theo tài khoản admin của bạn
                    password: 'password123' // Thay đổi theo mật khẩu admin của bạn
                })
            });

            if (response.ok) {
                const data = await response.json();
                this.authToken = data.token;
                return true;
            } else {
                console.error('❌ Đăng nhập thất bại:', response.status);
                return false;
            }
        } catch (error) {
            console.error('❌ Lỗi đăng nhập:', error.message);
            return false;
        }
    }

    /**
     * Tạo dữ liệu mẫu cho book titles
     */
    generateBookTitle(index) {
        const title = this.sampleTitles[Math.floor(Math.random() * this.sampleTitles.length)];
        const author = this.sampleAuthors[Math.floor(Math.random() * this.sampleAuthors.length)];
        const publisher = this.samplePublishers[Math.floor(Math.random() * this.samplePublishers.length)];
        const category = this.sampleCategories[Math.floor(Math.random() * this.sampleCategories.length)];

        return {
            isbn: `978-${String(Math.floor(Math.random() * 1000000000)).padStart(9, '0')}`,
            title: `${title} - Phiên bản ${index + 1}`,
            author: author,
            publisher: publisher,
            publishedYear: Math.floor(Math.random() * 25) + 2000, // 2000-2024
            category: category,
            description: `Mô tả chi tiết về cuốn sách "${title}" của tác giả ${author}. Đây là một tài liệu hữu ích cho việc học tập và nghiên cứu.`,
            language: 'vi',
            totalPages: Math.floor(Math.random() * 500) + 100 // 100-600 trang
        };
    }

    /**
     * Tạo dữ liệu mẫu cho book items
     */
    generateBookItem(bookTitleId, index) {
        const condition = this.sampleConditions[Math.floor(Math.random() * this.sampleConditions.length)];
        const status = this.sampleStatuses[Math.floor(Math.random() * this.sampleStatuses.length)];
        const shelfLocation = this.sampleShelfLocations[Math.floor(Math.random() * this.sampleShelfLocations.length)];

        return {
            bookTitleId: bookTitleId,
            libraryId: 1, // Giả sử có thư viện với ID = 1
            itemCode: `ITEM-${String(index + 1).padStart(8, '0')}`,
            shelfLocation: `${shelfLocation}-${Math.floor(Math.random() * 100)}`,
            condition: condition,
            status: status,
            acquisitionDate: this.getRandomDate(),
            acquisitionCost: Math.floor(Math.random() * 500000) + 50000, // 50k-550k VND
            notes: `Ghi chú cho item ${index + 1}`
        };
    }

    /**
     * Tạo ngày ngẫu nhiên trong 2 năm gần đây
     */
    getRandomDate() {
        const start = new Date(2022, 0, 1);
        const end = new Date();
        const randomTime = start.getTime() + Math.random() * (end.getTime() - start.getTime());
        return new Date(randomTime).toISOString().split('T')[0];
    }

    /**
     * Tạo book title qua API
     */
    async createBookTitle(bookData) {
        try {
            const response = await fetch(`${this.baseUrl}/books`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.authToken}`
                },
                body: JSON.stringify(bookData)
            });

            if (response.ok) {
                const result = await response.json();
                return result.id;
            } else {
                console.error('❌ Lỗi tạo book title:', response.status, await response.text());
                return null;
            }
        } catch (error) {
            console.error('❌ Lỗi tạo book title:', error.message);
            return null;
        }
    }

    /**
     * Tạo book item qua API
     */
    async createBookItem(itemData) {
        try {
            const response = await fetch(`${this.baseUrl}/book-items`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.authToken}`
                },
                body: JSON.stringify(itemData)
            });

            if (response.ok) {
                const result = await response.json();
                return result.id;
            } else {
                console.error('❌ Lỗi tạo book item:', response.status, await response.text());
                return null;
            }
        } catch (error) {
            console.error('❌ Lỗi tạo book item:', error.message);
            return null;
        }
    }

    /**
     * Tạo dữ liệu mock với số lượng sách cụ thể
     */
    async generateMockData(totalBooks) {

        const startTime = Date.now();
        let successCount = 0;
        let errorCount = 0;

        // Tạo batch để tránh quá tải
        const batchSize = 100;
        const totalBatches = Math.ceil(totalBooks / batchSize);

        for (let batch = 0; batch < totalBatches; batch++) {
            const batchStart = batch * batchSize;
            const batchEnd = Math.min(batchStart + batchSize, totalBooks);
            const currentBatchSize = batchEnd - batchStart;


            // Tạo book titles cho batch này
            const bookTitlePromises = [];
            for (let i = batchStart; i < batchEnd; i++) {
                const bookData = this.generateBookTitle(i);
                bookTitlePromises.push(this.createBookTitle(bookData));
            }

            const bookTitleIds = await Promise.all(bookTitlePromises);

            // Tạo book items cho batch này
            const bookItemPromises = [];
            for (let i = 0; i < bookTitleIds.length; i++) {
                if (bookTitleIds[i]) {
                    const itemData = this.generateBookItem(bookTitleIds[i], batchStart + i);
                    bookItemPromises.push(this.createBookItem(itemData));
                }
            }

            const bookItemIds = await Promise.all(bookItemPromises);

            // Đếm kết quả
            const batchSuccess = bookItemIds.filter(id => id !== null).length;
            const batchError = bookItemIds.length - batchSuccess;

            successCount += batchSuccess;
            errorCount += batchError;


            // Nghỉ ngắn giữa các batch để tránh quá tải server
            if (batch < totalBatches - 1) {
                await new Promise(resolve => setTimeout(resolve, 5));
            }
        }

        const endTime = Date.now();
        const duration = (endTime - startTime) / 1000;


        return {
            totalBooks,
            successCount,
            errorCount,
            duration,
            rate: successCount / duration
        };
    }

    /**
     * Lưu kết quả vào file
     */
    saveResults(results, filename) {
        const data = {
            timestamp: new Date().toISOString(),
            results: results
        };

        fs.writeFileSync(filename, JSON.stringify(data, null, 2));
    }
}

// Hàm chính để chạy generator
async function main() {
    const generator = new MockDataGenerator();

    // Đăng nhập
    const loginSuccess = await generator.login();
    if (!loginSuccess) {
        console.error('❌ Không thể đăng nhập. Vui lòng kiểm tra thông tin đăng nhập.');
        return;
    }

    // Menu lựa chọn
    const args = process.argv.slice(2);
    let totalBooks = 1000; // Mặc định 1000 quyển

    if (args.length > 0) {
        const input = args[0].toLowerCase();
        if (input.includes('1m')) {
            totalBooks = 1000000;
        } else if (input.includes('10m')) {
            totalBooks = 10000000;
        } else if (input.includes('100m')) {
            totalBooks = 100000000;
        } else {
            totalBooks = parseInt(input) || 1000;
        }
    }


    // Xác nhận từ người dùng
    if (totalBooks > 10000) {
        await new Promise(resolve => setTimeout(resolve, 5000));
    }

    // Tạo dữ liệu
    const results = await generator.generateMockData(totalBooks);

    // Lưu kết quả
    const filename = `mock-data-results-${totalBooks}-${Date.now()}.json`;
    generator.saveResults(results, filename);
}

// Chạy nếu file được gọi trực tiếp
if (require.main === module) {
    main().catch(console.error);
}

module.exports = MockDataGenerator;
