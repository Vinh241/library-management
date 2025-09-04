/**
 * Database Performance Tester cho hệ thống quản lý thư viện
 * Test khả năng lưu trữ và truy vấn database với dữ liệu lớn
 */

const fs = require('fs');
const path = require('path');
const { Client } = require('pg');

class DatabaseTester {
    constructor() {
        this.dbConfig = {
            host: 'localhost',
            port: 5433,
            database: 'postgres',
            user: 'postgres',
            password: 'rand0mPassw0rdforDB'
        };
        this.client = null;
        this.results = [];
    }

    /**
     * Kết nối database
     */
    async connect() {
        try {
            this.client = new Client(this.dbConfig);
            await this.client.connect();
            return true;
        } catch (error) {
            console.error('❌ Lỗi kết nối database:', error.message);
            return false;
        }
    }

    /**
     * Đóng kết nối database
     */
    async disconnect() {
        if (this.client) {
            await this.client.end();
        }
    }

    /**
     * Đo thời gian thực thi query
     */
    async measureQuery(query, params = []) {
        const startTime = Date.now();
        let success = false;
        let rowCount = 0;
        let error = null;

        try {
            const result = await this.client.query(query, params);
            success = true;
            rowCount = result.rowCount || 0;
        } catch (err) {
            error = err.message;
        }

        const endTime = Date.now();
        const duration = endTime - startTime;

        return {
            query: query.substring(0, 100) + (query.length > 100 ? '...' : ''),
            duration,
            success,
            rowCount,
            error,
            timestamp: new Date().toISOString()
        };
    }

    /**
     * Test performance của các query cơ bản
     */
    async testBasicQueries() {

        const queries = [
            {
                name: 'Count All Books',
                query: 'SELECT COUNT(*) FROM book_titles',
                description: 'Đếm tổng số book titles'
            },
            {
                name: 'Count All Book Items',
                query: 'SELECT COUNT(*) FROM book_items',
                description: 'Đếm tổng số book items'
            },
            {
                name: 'Count All Borrow Requests',
                query: 'SELECT COUNT(*) FROM borrow_requests',
                description: 'Đếm tổng số borrow requests'
            },
            {
                name: 'Count All Borrowing Transactions',
                query: 'SELECT COUNT(*) FROM borrowing_transactions',
                description: 'Đếm tổng số borrowing transactions'
            },
            {
                name: 'Count All Accounts',
                query: 'SELECT COUNT(*) FROM accounts',
                description: 'Đếm tổng số accounts'
            },
            {
                name: 'Count All Libraries',
                query: 'SELECT COUNT(*) FROM libraries',
                description: 'Đếm tổng số libraries'
            }
        ];

        const results = [];

        for (const query of queries) {
            const result = await this.measureQuery(query.query);
            result.name = query.name;
            result.description = query.description;
            results.push(result);

            if (result.success) {
            } else {
            }
        }

        return results;
    }

    /**
     * Test performance của các query tìm kiếm
     */
    async testSearchQueries() {

        const searchQueries = [
            {
                name: 'Search Books by Title',
                query: `SELECT * FROM book_titles WHERE title ILIKE $1 LIMIT 20`,
                params: ['%java%'],
                description: 'Tìm kiếm sách theo title'
            },
            {
                name: 'Search Books by Author',
                query: `SELECT * FROM book_titles WHERE author ILIKE $1 LIMIT 20`,
                params: ['%nguyen%'],
                description: 'Tìm kiếm sách theo author'
            },
            {
                name: 'Search Books by Category',
                query: `SELECT * FROM book_titles WHERE category = $1 LIMIT 20`,
                params: ['Lập trình'],
                description: 'Tìm kiếm sách theo category'
            },
            {
                name: 'Search Book Items by Status',
                query: `SELECT * FROM book_items WHERE status = $1 LIMIT 20`,
                params: ['AVAILABLE'],
                description: 'Tìm kiếm book items theo status'
            },
            {
                name: 'Search Book Items by Condition',
                query: `SELECT * FROM book_items WHERE condition = $1 LIMIT 20`,
                params: ['GOOD'],
                description: 'Tìm kiếm book items theo condition'
            },
            {
                name: 'Search Borrow Requests by Status',
                query: `SELECT * FROM borrow_requests WHERE status = $1 LIMIT 20`,
                params: ['PENDING'],
                description: 'Tìm kiếm borrow requests theo status'
            }
        ];

        const results = [];

        for (const query of searchQueries) {
            const result = await this.measureQuery(query.query, query.params);
            result.name = query.name;
            result.description = query.description;
            results.push(result);

            if (result.success) {
            } else {
            }
        }

        return results;
    }

    /**
     * Test performance của các query phức tạp với JOIN
     */
    async testComplexQueries() {

        const complexQueries = [
            {
                name: 'Books with Items Count',
                query: `
                    SELECT bt.id, bt.title, bt.author, COUNT(bi.id) as item_count
                    FROM book_titles bt
                    LEFT JOIN book_items bi ON bt.id = bi.book_title_id
                    GROUP BY bt.id, bt.title, bt.author
                    ORDER BY item_count DESC
                    LIMIT 20
                `,
                description: 'Đếm số lượng items cho mỗi book title'
            },
            {
                name: 'Available Books in Libraries',
                query: `
                    SELECT l.name as library_name, bt.title, bt.author, COUNT(bi.id) as available_count
                    FROM libraries l
                    JOIN book_items bi ON l.id = bi.library_id
                    JOIN book_titles bt ON bi.book_title_id = bt.id
                    WHERE bi.status = 'AVAILABLE'
                    GROUP BY l.id, l.name, bt.id, bt.title, bt.author
                    ORDER BY available_count DESC
                    LIMIT 20
                `,
                description: 'Sách có sẵn trong các thư viện'
            },
            {
                name: 'User Borrow History',
                query: `
                    SELECT a.username, a.full_name, COUNT(bt.id) as borrow_count
                    FROM accounts a
                    LEFT JOIN borrowing_transactions bt ON a.id = bt.borrower_id
                    GROUP BY a.id, a.username, a.full_name
                    ORDER BY borrow_count DESC
                    LIMIT 20
                `,
                description: 'Lịch sử mượn sách của users'
            },
            {
                name: 'Overdue Books',
                query: `
                    SELECT bt.title, a.full_name, bt.borrow_date, bt.due_date
                    FROM borrowing_transactions bt
                    JOIN book_items bi ON bt.book_item_id = bi.id
                    JOIN book_titles btt ON bi.book_title_id = btt.id
                    JOIN accounts a ON bt.borrower_id = a.id
                    WHERE bt.status = 'ACTIVE' AND bt.due_date < CURRENT_DATE
                    ORDER BY bt.due_date ASC
                    LIMIT 20
                `,
                description: 'Sách quá hạn trả'
            },
            {
                name: 'Popular Books',
                query: `
                    SELECT bt.title, bt.author, COUNT(br.id) as request_count
                    FROM book_titles bt
                    JOIN book_items bi ON bt.id = bi.book_title_id
                    JOIN borrow_requests br ON bi.id = br.book_item_id
                    GROUP BY bt.id, bt.title, bt.author
                    ORDER BY request_count DESC
                    LIMIT 20
                `,
                description: 'Sách được yêu cầu mượn nhiều nhất'
            }
        ];

        const results = [];

        for (const query of complexQueries) {
            const result = await this.measureQuery(query.query);
            result.name = query.name;
            result.description = query.description;
            results.push(result);

            if (result.success) {
            } else {
            }
        }

        return results;
    }

    /**
     * Test performance của pagination
     */
    async testPaginationPerformance() {

        const paginationTests = [
            {
                name: 'Books Pagination - Page 1',
                query: 'SELECT * FROM book_titles ORDER BY title LIMIT 20 OFFSET 0',
                description: 'Trang đầu tiên của danh sách sách'
            },
            {
                name: 'Books Pagination - Page 10',
                query: 'SELECT * FROM book_titles ORDER BY title LIMIT 20 OFFSET 180',
                description: 'Trang 10 của danh sách sách'
            },
            {
                name: 'Books Pagination - Page 100',
                query: 'SELECT * FROM book_titles ORDER BY title LIMIT 20 OFFSET 1980',
                description: 'Trang 100 của danh sách sách'
            },
            {
                name: 'Books Pagination - Page 1000',
                query: 'SELECT * FROM book_titles ORDER BY title LIMIT 20 OFFSET 19980',
                description: 'Trang 1000 của danh sách sách'
            },
            {
                name: 'Book Items Pagination - Page 1',
                query: 'SELECT * FROM book_items ORDER BY item_code LIMIT 20 OFFSET 0',
                description: 'Trang đầu tiên của danh sách book items'
            },
            {
                name: 'Book Items Pagination - Page 100',
                query: 'SELECT * FROM book_items ORDER BY item_code LIMIT 20 OFFSET 1980',
                description: 'Trang 100 của danh sách book items'
            }
        ];

        const results = [];

        for (const test of paginationTests) {
            const result = await this.measureQuery(test.query);
            result.name = test.name;
            result.description = test.description;
            results.push(result);

            if (result.success) {
            } else {
            }
        }

        return results;
    }

    /**
     * Test performance của các query với index
     */
    async testIndexPerformance() {

        const indexTests = [
            {
                name: 'Search by ISBN (Indexed)',
                query: 'SELECT * FROM book_titles WHERE isbn = $1',
                params: ['978-123456789'],
                description: 'Tìm kiếm theo ISBN (có unique index)'
            },
            {
                name: 'Search by Item Code (Indexed)',
                query: 'SELECT * FROM book_items WHERE item_code = $1',
                params: ['ITEM-00000001'],
                description: 'Tìm kiếm theo item code (có unique index)'
            },
            {
                name: 'Search by Username (Indexed)',
                query: 'SELECT * FROM accounts WHERE username = $1',
                params: ['admin'],
                description: 'Tìm kiếm theo username (có unique index)'
            },
            {
                name: 'Filter by Status (Indexed)',
                query: 'SELECT * FROM book_items WHERE status = $1 LIMIT 100',
                params: ['AVAILABLE'],
                description: 'Lọc theo status (có index)'
            },
            {
                name: 'Filter by Due Date (Indexed)',
                query: 'SELECT * FROM borrowing_transactions WHERE due_date < CURRENT_DATE LIMIT 100',
                description: 'Lọc theo due date (có index)'
            }
        ];

        const results = [];

        for (const test of indexTests) {
            const result = await this.measureQuery(test.query, test.params);
            result.name = test.name;
            result.description = test.description;
            results.push(result);

            if (result.success) {
            } else {
            }
        }

        return results;
    }

    /**
     * Test performance của các query thống kê
     */
    async testAnalyticsQueries() {

        const analyticsQueries = [
            {
                name: 'Books by Category',
                query: `
                    SELECT category, COUNT(*) as book_count
                    FROM book_titles
                    WHERE category IS NOT NULL
                    GROUP BY category
                    ORDER BY book_count DESC
                `,
                description: 'Thống kê sách theo category'
            },
            {
                name: 'Books by Year',
                query: `
                    SELECT published_year, COUNT(*) as book_count
                    FROM book_titles
                    WHERE published_year IS NOT NULL
                    GROUP BY published_year
                    ORDER BY published_year DESC
                `,
                description: 'Thống kê sách theo năm xuất bản'
            },
            {
                name: 'Book Items by Status',
                query: `
                    SELECT status, COUNT(*) as item_count
                    FROM book_items
                    GROUP BY status
                    ORDER BY item_count DESC
                `,
                description: 'Thống kê book items theo status'
            },
            {
                name: 'Borrow Requests by Status',
                query: `
                    SELECT status, COUNT(*) as request_count
                    FROM borrow_requests
                    GROUP BY status
                    ORDER BY request_count DESC
                `,
                description: 'Thống kê borrow requests theo status'
            },
            {
                name: 'Monthly Borrow Statistics',
                query: `
                    SELECT 
                        DATE_TRUNC('month', created_at) as month,
                        COUNT(*) as borrow_count
                    FROM borrowing_transactions
                    GROUP BY DATE_TRUNC('month', created_at)
                    ORDER BY month DESC
                    LIMIT 12
                `,
                description: 'Thống kê mượn sách theo tháng'
            }
        ];

        const results = [];

        for (const query of analyticsQueries) {
            const result = await this.measureQuery(query.query);
            result.name = query.name;
            result.description = query.description;
            results.push(result);

            if (result.success) {
            } else {
            }
        }

        return results;
    }

    /**
     * Test concurrent queries
     */
    async testConcurrentQueries() {

        const concurrentQueries = [
            'SELECT COUNT(*) FROM book_titles',
            'SELECT COUNT(*) FROM book_items',
            'SELECT COUNT(*) FROM borrow_requests',
            'SELECT COUNT(*) FROM borrowing_transactions',
            'SELECT COUNT(*) FROM accounts'
        ];

        const concurrency = 10;
        const results = [];


        for (let i = 0; i < concurrency; i++) {
            const promises = concurrentQueries.map(query => this.measureQuery(query));
            const batchResults = await Promise.all(promises);
            results.push(...batchResults);
        }

        const successfulQueries = results.filter(r => r.success);
        const avgDuration = successfulQueries.reduce((sum, r) => sum + r.duration, 0) / successfulQueries.length;
        const maxDuration = Math.max(...successfulQueries.map(r => r.duration));
        const minDuration = Math.min(...successfulQueries.map(r => r.duration));


        return {
            name: 'Concurrent Queries Test',
            totalQueries: results.length,
            successfulQueries: successfulQueries.length,
            avgDuration: Math.round(avgDuration * 100) / 100,
            minDuration,
            maxDuration,
            results
        };
    }

    /**
     * Chạy tất cả database tests
     */
    async runAllTests() {

        const allResults = [];

        try {
            // Basic queries
            const basicResults = await this.testBasicQueries();
            allResults.push(...basicResults);

            // Search queries
            const searchResults = await this.testSearchQueries();
            allResults.push(...searchResults);

            // Complex queries
            const complexResults = await this.testComplexQueries();
            allResults.push(...complexResults);

            // Pagination tests
            const paginationResults = await this.testPaginationPerformance();
            allResults.push(...paginationResults);

            // Index tests
            const indexResults = await this.testIndexPerformance();
            allResults.push(...indexResults);

            // Analytics tests
            const analyticsResults = await this.testAnalyticsQueries();
            allResults.push(...analyticsResults);

            // Concurrent tests
            const concurrentResult = await this.testConcurrentQueries();
            allResults.push(concurrentResult);

        } catch (error) {
            console.error('❌ Lỗi trong quá trình test:', error.message);
        }

        return allResults;
    }

    /**
     * Tạo báo cáo tổng hợp
     */
    generateReport(allResults) {

        // Lọc kết quả hợp lệ
        const validResults = allResults.filter(r => r.duration !== undefined);

        if (validResults.length === 0) {
            return { summary: {}, details: allResults };
        }

        // Sắp xếp theo thời gian thực thi
        const sortedByDuration = [...validResults].sort((a, b) => a.duration - b.duration);

        sortedByDuration.slice(0, 5).forEach((result, index) => {
        });

        sortedByDuration.slice(-5).reverse().forEach((result, index) => {
        });

        // Thống kê tổng quan
        const totalQueries = validResults.length;
        const successfulQueries = validResults.filter(r => r.success).length;
        const avgDuration = validResults.reduce((sum, r) => sum + r.duration, 0) / totalQueries;
        const maxDuration = Math.max(...validResults.map(r => r.duration));
        const minDuration = Math.min(...validResults.map(r => r.duration));


        // Phân loại theo performance
        const fastQueries = validResults.filter(r => r.duration < 10).length;
        const mediumQueries = validResults.filter(r => r.duration >= 10 && r.duration < 100).length;
        const slowQueries = validResults.filter(r => r.duration >= 100).length;


        return {
            summary: {
                totalQueries,
                successfulQueries,
                avgDuration,
                maxDuration,
                minDuration,
                fastQueries,
                mediumQueries,
                slowQueries
            },
            details: allResults
        };
    }

    /**
     * Lưu kết quả vào file
     */
    saveResults(results, filename) {
        const data = {
            timestamp: new Date().toISOString(),
            testType: 'Database Performance Testing',
            results: results
        };

        fs.writeFileSync(filename, JSON.stringify(data, null, 2));
    }
}

// Hàm chính để chạy database testing
async function main() {
    const tester = new DatabaseTester();

    try {
        // Kết nối database
        const connected = await tester.connect();
        if (!connected) {
            console.error('❌ Không thể kết nối database. Vui lòng kiểm tra cấu hình.');
            return;
        }

        const args = process.argv.slice(2);
        const testType = args[0] || 'all';

        let allResults = [];

        switch (testType) {
            case 'all':
                allResults = await tester.runAllTests();
                break;

            case 'basic':
                allResults = await tester.testBasicQueries();
                break;

            case 'search':
                allResults = await tester.testSearchQueries();
                break;

            case 'complex':
                allResults = await tester.testComplexQueries();
                break;

            case 'pagination':
                allResults = await tester.testPaginationPerformance();
                break;

            case 'index':
                allResults = await tester.testIndexPerformance();
                break;

            case 'analytics':
                allResults = await tester.testAnalyticsQueries();
                break;

            case 'concurrent':
                const concurrentResult = await tester.testConcurrentQueries();
                allResults = [concurrentResult];
                break;

            default:
                return;
        }

        const report = tester.generateReport(allResults);


        const filename = `database-test-results-${testType}-${Date.now()}.json`;
        tester.saveResults(report, filename);

    } finally {
        // Đóng kết nối
        await tester.disconnect();
    }
}

// Chạy nếu file được gọi trực tiếp
if (require.main === module) {
    main().catch(console.error);
}

module.exports = DatabaseTester;
