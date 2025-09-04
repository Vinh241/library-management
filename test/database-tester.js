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
            console.log('✅ Kết nối database thành công');
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
            console.log('✅ Đã đóng kết nối database');
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
        console.log('🧪 Testing Basic Database Queries...\n');

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
            console.log(`   📊 ${query.name}...`);
            const result = await this.measureQuery(query.query);
            result.name = query.name;
            result.description = query.description;
            results.push(result);

            if (result.success) {
                console.log(`      ✅ ${result.duration}ms - ${result.rowCount} rows`);
            } else {
                console.log(`      ❌ ${result.duration}ms - Error: ${result.error}`);
            }
        }

        return results;
    }

    /**
     * Test performance của các query tìm kiếm
     */
    async testSearchQueries() {
        console.log('\n🔍 Testing Search Queries...\n');

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
            console.log(`   🔍 ${query.name}...`);
            const result = await this.measureQuery(query.query, query.params);
            result.name = query.name;
            result.description = query.description;
            results.push(result);

            if (result.success) {
                console.log(`      ✅ ${result.duration}ms - ${result.rowCount} rows`);
            } else {
                console.log(`      ❌ ${result.duration}ms - Error: ${result.error}`);
            }
        }

        return results;
    }

    /**
     * Test performance của các query phức tạp với JOIN
     */
    async testComplexQueries() {
        console.log('\n🔗 Testing Complex Queries with JOINs...\n');

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
            console.log(`   🔗 ${query.name}...`);
            const result = await this.measureQuery(query.query);
            result.name = query.name;
            result.description = query.description;
            results.push(result);

            if (result.success) {
                console.log(`      ✅ ${result.duration}ms - ${result.rowCount} rows`);
            } else {
                console.log(`      ❌ ${result.duration}ms - Error: ${result.error}`);
            }
        }

        return results;
    }

    /**
     * Test performance của pagination
     */
    async testPaginationPerformance() {
        console.log('\n📄 Testing Pagination Performance...\n');

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
            console.log(`   📄 ${test.name}...`);
            const result = await this.measureQuery(test.query);
            result.name = test.name;
            result.description = test.description;
            results.push(result);

            if (result.success) {
                console.log(`      ✅ ${result.duration}ms - ${result.rowCount} rows`);
            } else {
                console.log(`      ❌ ${result.duration}ms - Error: ${result.error}`);
            }
        }

        return results;
    }

    /**
     * Test performance của các query với index
     */
    async testIndexPerformance() {
        console.log('\n📊 Testing Index Performance...\n');

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
            console.log(`   📊 ${test.name}...`);
            const result = await this.measureQuery(test.query, test.params);
            result.name = test.name;
            result.description = test.description;
            results.push(result);

            if (result.success) {
                console.log(`      ✅ ${result.duration}ms - ${result.rowCount} rows`);
            } else {
                console.log(`      ❌ ${result.duration}ms - Error: ${result.error}`);
            }
        }

        return results;
    }

    /**
     * Test performance của các query thống kê
     */
    async testAnalyticsQueries() {
        console.log('\n📈 Testing Analytics Queries...\n');

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
            console.log(`   📈 ${query.name}...`);
            const result = await this.measureQuery(query.query);
            result.name = query.name;
            result.description = query.description;
            results.push(result);

            if (result.success) {
                console.log(`      ✅ ${result.duration}ms - ${result.rowCount} rows`);
            } else {
                console.log(`      ❌ ${result.duration}ms - Error: ${result.error}`);
            }
        }

        return results;
    }

    /**
     * Test concurrent queries
     */
    async testConcurrentQueries() {
        console.log('\n⚡ Testing Concurrent Queries...\n');

        const concurrentQueries = [
            'SELECT COUNT(*) FROM book_titles',
            'SELECT COUNT(*) FROM book_items',
            'SELECT COUNT(*) FROM borrow_requests',
            'SELECT COUNT(*) FROM borrowing_transactions',
            'SELECT COUNT(*) FROM accounts'
        ];

        const concurrency = 10;
        const results = [];

        console.log(`   🚀 Chạy ${concurrentQueries.length} queries với concurrency ${concurrency}...`);

        for (let i = 0; i < concurrency; i++) {
            const promises = concurrentQueries.map(query => this.measureQuery(query));
            const batchResults = await Promise.all(promises);
            results.push(...batchResults);
        }

        const successfulQueries = results.filter(r => r.success);
        const avgDuration = successfulQueries.reduce((sum, r) => sum + r.duration, 0) / successfulQueries.length;
        const maxDuration = Math.max(...successfulQueries.map(r => r.duration));
        const minDuration = Math.min(...successfulQueries.map(r => r.duration));

        console.log(`   ✅ Total queries: ${results.length}`);
        console.log(`   ✅ Successful: ${successfulQueries.length}`);
        console.log(`   ⏱️  Avg duration: ${avgDuration.toFixed(2)}ms`);
        console.log(`   📊 Min/Max: ${minDuration}ms / ${maxDuration}ms`);

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
        console.log('🚀 Bắt đầu Database Performance Testing...\n');

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
        console.log('\n📊 BÁO CÁO TỔNG HỢP DATABASE PERFORMANCE\n');
        console.log('='.repeat(80));

        // Lọc kết quả hợp lệ
        const validResults = allResults.filter(r => r.duration !== undefined);

        if (validResults.length === 0) {
            console.log('❌ Không có kết quả hợp lệ để phân tích');
            return { summary: {}, details: allResults };
        }

        // Sắp xếp theo thời gian thực thi
        const sortedByDuration = [...validResults].sort((a, b) => a.duration - b.duration);

        console.log('\n🏆 TOP 5 QUERIES NHANH NHẤT:');
        sortedByDuration.slice(0, 5).forEach((result, index) => {
            console.log(`${index + 1}. ${result.name}: ${result.duration}ms`);
        });

        console.log('\n🐌 TOP 5 QUERIES CHẬM NHẤT:');
        sortedByDuration.slice(-5).reverse().forEach((result, index) => {
            console.log(`${index + 1}. ${result.name}: ${result.duration}ms`);
        });

        // Thống kê tổng quan
        const totalQueries = validResults.length;
        const successfulQueries = validResults.filter(r => r.success).length;
        const avgDuration = validResults.reduce((sum, r) => sum + r.duration, 0) / totalQueries;
        const maxDuration = Math.max(...validResults.map(r => r.duration));
        const minDuration = Math.min(...validResults.map(r => r.duration));

        console.log('\n📈 THỐNG KÊ TỔNG QUAN:');
        console.log(`   Tổng số queries: ${totalQueries}`);
        console.log(`   Queries thành công: ${successfulQueries}/${totalQueries} (${Math.round(successfulQueries / totalQueries * 100)}%)`);
        console.log(`   Thời gian trung bình: ${avgDuration.toFixed(2)}ms`);
        console.log(`   Thời gian nhanh nhất: ${minDuration}ms`);
        console.log(`   Thời gian chậm nhất: ${maxDuration}ms`);

        // Phân loại theo performance
        const fastQueries = validResults.filter(r => r.duration < 10).length;
        const mediumQueries = validResults.filter(r => r.duration >= 10 && r.duration < 100).length;
        const slowQueries = validResults.filter(r => r.duration >= 100).length;

        console.log('\n⚡ PHÂN LOẠI PERFORMANCE:');
        console.log(`   Nhanh (<10ms): ${fastQueries} queries`);
        console.log(`   Trung bình (10-100ms): ${mediumQueries} queries`);
        console.log(`   Chậm (>100ms): ${slowQueries} queries`);

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
        console.log(`\n💾 Kết quả đã được lưu vào ${filename}`);
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
                console.log('🎯 Chạy tất cả database tests...\n');
                allResults = await tester.runAllTests();
                break;

            case 'basic':
                console.log('🎯 Chạy basic query tests...\n');
                allResults = await tester.testBasicQueries();
                break;

            case 'search':
                console.log('🎯 Chạy search query tests...\n');
                allResults = await tester.testSearchQueries();
                break;

            case 'complex':
                console.log('🎯 Chạy complex query tests...\n');
                allResults = await tester.testComplexQueries();
                break;

            case 'pagination':
                console.log('🎯 Chạy pagination tests...\n');
                allResults = await tester.testPaginationPerformance();
                break;

            case 'index':
                console.log('🎯 Chạy index performance tests...\n');
                allResults = await tester.testIndexPerformance();
                break;

            case 'analytics':
                console.log('🎯 Chạy analytics tests...\n');
                allResults = await tester.testAnalyticsQueries();
                break;

            case 'concurrent':
                console.log('🎯 Chạy concurrent query tests...\n');
                const concurrentResult = await tester.testConcurrentQueries();
                allResults = [concurrentResult];
                break;

            default:
                console.log('❌ Loại test không hợp lệ. Sử dụng: all, basic, search, complex, pagination, index, analytics, concurrent');
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
