/**
 * Performance Tester cho hệ thống quản lý thư viện
 * Test độ trễ API và performance của các endpoints
 */

const fs = require('fs');
const path = require('path');

class PerformanceTester {
    constructor() {
        this.baseUrl = 'http://localhost:8080/api';
        this.authToken = null;
        this.results = [];
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
                    username: 'admin',
                    password: 'password123'
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
     * Đo thời gian response của một API call
     */
    async measureApiCall(name, url, options = {}) {
        const startTime = Date.now();
        let success = false;
        let statusCode = 0;
        let responseSize = 0;
        let error = null;

        try {
            const response = await fetch(url, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.authToken}`,
                    ...options.headers
                }
            });

            statusCode = response.status;
            success = response.ok;

            if (response.ok) {
                const data = await response.text();
                responseSize = data.length;
            }
        } catch (err) {
            error = err.message;
        }

        const endTime = Date.now();
        const duration = endTime - startTime;

        return {
            name,
            url,
            duration,
            success,
            statusCode,
            responseSize,
            error,
            timestamp: new Date().toISOString()
        };
    }

    /**
     * Test performance của một endpoint với nhiều lần gọi
     */
    async testEndpointPerformance(name, url, options = {}, iterations = 10) {

        const results = [];
        let successCount = 0;
        let totalDuration = 0;
        let minDuration = Infinity;
        let maxDuration = 0;
        let totalResponseSize = 0;

        for (let i = 0; i < iterations; i++) {
            const result = await this.measureApiCall(name, url, options);
            results.push(result);

            if (result.success) {
                successCount++;
                totalDuration += result.duration;
                minDuration = Math.min(minDuration, result.duration);
                maxDuration = Math.max(maxDuration, result.duration);
                totalResponseSize += result.responseSize;
            }

            // Hiển thị progress
            if ((i + 1) % Math.max(1, Math.floor(iterations / 10)) === 0) {
                process.stdout.write(`\r   Progress: ${i + 1}/${iterations} (${Math.round((i + 1) / iterations * 100)}%)`);
            }
        }

        // New line after progress

        const avgDuration = successCount > 0 ? totalDuration / successCount : 0;
        const successRate = (successCount / iterations) * 100;
        const avgResponseSize = successCount > 0 ? totalResponseSize / successCount : 0;

        const summary = {
            name,
            url,
            iterations,
            successCount,
            successRate: Math.round(successRate * 100) / 100,
            avgDuration: Math.round(avgDuration * 100) / 100,
            minDuration,
            maxDuration,
            avgResponseSize: Math.round(avgResponseSize),
            totalResponseSize,
            results
        };


        return summary;
    }

    /**
     * Test tất cả các endpoints chính
     */
    async runAllTests() {

        const tests = [
            // Book Title Tests
            {
                name: 'Get All Books (Page 1)',
                url: `${this.baseUrl}/books?page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Get All Books (Page 10)',
                url: `${this.baseUrl}/books?page=9&size=20`,
                method: 'GET'
            },
            {
                name: 'Search Books',
                url: `${this.baseUrl}/books/search?keyword=java&page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Advanced Search Books',
                url: `${this.baseUrl}/books/advanced-search?title=java&author=robert&page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Get Book by ID',
                url: `${this.baseUrl}/books/1`,
                method: 'GET'
            },
            {
                name: 'Get Book by ISBN',
                url: `${this.baseUrl}/books/isbn/978-123456789`,
                method: 'GET'
            },

            // Book Item Tests
            {
                name: 'Get All Book Items (Page 1)',
                url: `${this.baseUrl}/book-items?page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Get All Book Items (Page 10)',
                url: `${this.baseUrl}/book-items?page=9&size=20`,
                method: 'GET'
            },
            {
                name: 'Search Book Items',
                url: `${this.baseUrl}/book-items/search?status=AVAILABLE&page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Get Book Items by Status',
                url: `${this.baseUrl}/book-items/status/AVAILABLE?page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Get Book Item by ID',
                url: `${this.baseUrl}/book-items/1`,
                method: 'GET'
            },
            {
                name: 'Get Book Item by Code',
                url: `${this.baseUrl}/book-items/code/ITEM-00000001`,
                method: 'GET'
            },

            // Borrow Request Tests
            {
                name: 'Get My Borrow Requests',
                url: `${this.baseUrl}/borrow-requests/my-requests?page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Get Pending Requests for Review',
                url: `${this.baseUrl}/borrow-requests/pending-review?page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Get Library Borrow Requests',
                url: `${this.baseUrl}/borrow-requests/library/1?page=0&size=20`,
                method: 'GET'
            },
            {
                name: 'Check Has Pending Request',
                url: `${this.baseUrl}/borrow-requests/has-pending`,
                method: 'GET'
            }
        ];

        const allResults = [];

        for (const test of tests) {
            try {
                const result = await this.testEndpointPerformance(
                    test.name,
                    test.url,
                    { method: test.method },
                    20 // 20 iterations per test
                );
                allResults.push(result);
                // Empty line between tests
            } catch (error) {
                console.error(`❌ Lỗi test ${test.name}:`, error.message);
                allResults.push({
                    name: test.name,
                    url: test.url,
                    error: error.message,
                    timestamp: new Date().toISOString()
                });
            }
        }

        return allResults;
    }

    /**
     * Test performance với các kích thước page khác nhau
     */
    async testPaginationPerformance() {

        const pageSizes = [10, 20, 50, 100, 200];
        const results = [];

        for (const size of pageSizes) {

            const result = await this.testEndpointPerformance(
                `Books Pagination (Size ${size})`,
                `${this.baseUrl}/books?page=0&size=${size}`,
                { method: 'GET' },
                10
            );

            results.push(result);
        }

        return results;
    }

    /**
     * Test performance với các từ khóa tìm kiếm khác nhau
     */
    async testSearchPerformance() {

        const keywords = ['java', 'spring', 'database', 'programming', 'development'];
        const results = [];

        for (const keyword of keywords) {

            const result = await this.testEndpointPerformance(
                `Search Books (Keyword: ${keyword})`,
                `${this.baseUrl}/books/search?keyword=${keyword}&page=0&size=20`,
                { method: 'GET' },
                10
            );

            results.push(result);
        }

        return results;
    }

    /**
     * Tạo báo cáo tổng hợp
     */
    generateReport(allResults) {

        // Sắp xếp theo thời gian trung bình
        const sortedResults = allResults
            .filter(r => r.avgDuration !== undefined)
            .sort((a, b) => a.avgDuration - b.avgDuration);

        sortedResults.slice(0, 5).forEach((result, index) => {
        });

        sortedResults.slice(-5).reverse().forEach((result, index) => {
        });

        // Thống kê tổng quan
        const totalTests = allResults.length;
        const successfulTests = allResults.filter(r => r.successRate >= 95).length;
        const avgResponseTime = sortedResults.reduce((sum, r) => sum + r.avgDuration, 0) / sortedResults.length;
        const maxResponseTime = Math.max(...sortedResults.map(r => r.avgDuration));
        const minResponseTime = Math.min(...sortedResults.map(r => r.avgDuration));


        // Phân loại theo performance
        const fastEndpoints = sortedResults.filter(r => r.avgDuration < 100).length;
        const mediumEndpoints = sortedResults.filter(r => r.avgDuration >= 100 && r.avgDuration < 500).length;
        const slowEndpoints = sortedResults.filter(r => r.avgDuration >= 500).length;


        return {
            summary: {
                totalTests,
                successfulTests,
                avgResponseTime,
                maxResponseTime,
                minResponseTime,
                fastEndpoints,
                mediumEndpoints,
                slowEndpoints
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
            testType: 'Performance Testing',
            results: results
        };

        fs.writeFileSync(filename, JSON.stringify(data, null, 2));
    }
}

// Hàm chính để chạy performance testing
async function main() {
    const tester = new PerformanceTester();

    // Đăng nhập
    const loginSuccess = await tester.login();
    if (!loginSuccess) {
        console.error('❌ Không thể đăng nhập. Vui lòng kiểm tra thông tin đăng nhập.');
        return;
    }

    const args = process.argv.slice(2);
    const testType = args[0] || 'all';

    let allResults = [];

    switch (testType) {
        case 'all':
            allResults = await tester.runAllTests();
            break;

        case 'pagination':
            allResults = await tester.testPaginationPerformance();
            break;

        case 'search':
            allResults = await tester.testSearchPerformance();
            break;

        default:
            return;
    }

    // Tạo báo cáo
    const report = tester.generateReport(allResults);

    // Lưu kết quả
    const filename = `performance-test-results-${testType}-${Date.now()}.json`;
    tester.saveResults(report, filename);
}

// Chạy nếu file được gọi trực tiếp
if (require.main === module) {
    main().catch(console.error);
}

module.exports = PerformanceTester;
