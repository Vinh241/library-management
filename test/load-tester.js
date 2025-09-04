/**
 * Load Tester cho hệ thống quản lý thư viện
 * Test khả năng chịu tải với nhiều concurrent requests
 */

const fs = require('fs');
const path = require('path');

class LoadTester {
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
     * Tạo một request đơn lẻ
     */
    async makeRequest(url, options = {}) {
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
            duration,
            success,
            statusCode,
            responseSize,
            error,
            timestamp: new Date().toISOString()
        };
    }

    /**
     * Tạo nhiều concurrent requests
     */
    async createConcurrentRequests(url, options = {}, concurrency = 10, totalRequests = 100) {

        const results = [];
        const batches = Math.ceil(totalRequests / concurrency);

        for (let batch = 0; batch < batches; batch++) {
            const batchStart = batch * concurrency;
            const batchEnd = Math.min(batchStart + concurrency, totalRequests);
            const currentBatchSize = batchEnd - batchStart;

            // Tạo promises cho batch hiện tại
            const promises = [];
            for (let i = 0; i < currentBatchSize; i++) {
                promises.push(this.makeRequest(url, options));
            }

            // Chờ tất cả requests trong batch hoàn thành
            const batchResults = await Promise.all(promises);
            results.push(...batchResults);

            // Hiển thị progress
            const completed = Math.min(batchEnd, totalRequests);
            const progress = Math.round((completed / totalRequests) * 100);
            process.stdout.write(`\r   Progress: ${completed}/${totalRequests} (${progress}%)`);

            // Nghỉ ngắn giữa các batch để tránh quá tải
            if (batch < batches - 1) {
                await new Promise(resolve => setTimeout(resolve, 100));
            }
        }

        // New line after progress
        return results;
    }

    /**
     * Test load cho một endpoint cụ thể
     */
    async testEndpointLoad(name, url, options = {}, concurrency = 10, totalRequests = 100) {

        const startTime = Date.now();
        const results = await this.createConcurrentRequests(url, options, concurrency, totalRequests);
        const endTime = Date.now();

        // Phân tích kết quả
        const successfulRequests = results.filter(r => r.success).length;
        const failedRequests = results.length - successfulRequests;
        const successRate = (successfulRequests / results.length) * 100;

        const durations = results.filter(r => r.success).map(r => r.duration);
        const avgDuration = durations.length > 0 ? durations.reduce((a, b) => a + b, 0) / durations.length : 0;
        const minDuration = durations.length > 0 ? Math.min(...durations) : 0;
        const maxDuration = durations.length > 0 ? Math.max(...durations) : 0;

        // Tính percentile
        const sortedDurations = durations.sort((a, b) => a - b);
        const p50 = this.calculatePercentile(sortedDurations, 50);
        const p90 = this.calculatePercentile(sortedDurations, 90);
        const p95 = this.calculatePercentile(sortedDurations, 95);
        const p99 = this.calculatePercentile(sortedDurations, 99);

        const totalDuration = (endTime - startTime) / 1000; // seconds
        const requestsPerSecond = results.length / totalDuration;

        const summary = {
            name,
            url,
            concurrency,
            totalRequests,
            successfulRequests,
            failedRequests,
            successRate: Math.round(successRate * 100) / 100,
            avgDuration: Math.round(avgDuration * 100) / 100,
            minDuration,
            maxDuration,
            p50,
            p90,
            p95,
            p99,
            requestsPerSecond: Math.round(requestsPerSecond * 100) / 100,
            totalDuration: Math.round(totalDuration * 100) / 100,
            results
        };

        //         //         //         //         // 
        return summary;
    }

    /**
     * Tính percentile
     */
    calculatePercentile(sortedArray, percentile) {
        if (sortedArray.length === 0) return 0;
        const index = (percentile / 100) * (sortedArray.length - 1);
        const lower = Math.floor(index);
        const upper = Math.ceil(index);
        const weight = index % 1;

        if (upper >= sortedArray.length) return sortedArray[sortedArray.length - 1];
        return sortedArray[lower] * (1 - weight) + sortedArray[upper] * weight;
    }

    /**
     * Test load với các mức concurrency khác nhau
     */
    async testConcurrencyLevels(name, url, options = {}, concurrencyLevels = [1, 5, 10, 20, 50], requestsPerLevel = 100) {

        const results = [];

        for (const concurrency of concurrencyLevels) {
            const result = await this.testEndpointLoad(
                `${name} (Concurrency: ${concurrency})`,
                url,
                options,
                concurrency,
                requestsPerLevel
            );
            results.push(result);

            // Nghỉ giữa các test để server ổn định
            if (concurrency < concurrencyLevels[concurrencyLevels.length - 1]) {
                await new Promise(resolve => setTimeout(resolve, 5000));
            }
        }

        return results;
    }

    /**
     * Test load cho tất cả endpoints chính
     */
    async runAllLoadTests() {

        const tests = [
            {
                name: 'Get All Books',
                url: `${this.baseUrl}/books?page=0&size=20`,
                method: 'GET',
                concurrency: 20,
                totalRequests: 200
            },
            {
                name: 'Search Books',
                url: `${this.baseUrl}/books/search?keyword=java&page=0&size=20`,
                method: 'GET',
                concurrency: 15,
                totalRequests: 150
            },
            {
                name: 'Get All Book Items',
                url: `${this.baseUrl}/book-items?page=0&size=20`,
                method: 'GET',
                concurrency: 20,
                totalRequests: 200
            },
            {
                name: 'Search Book Items',
                url: `${this.baseUrl}/book-items/search?status=AVAILABLE&page=0&size=20`,
                method: 'GET',
                concurrency: 15,
                totalRequests: 150
            },
            {
                name: 'Get My Borrow Requests',
                url: `${this.baseUrl}/borrow-requests/my-requests?page=0&size=20`,
                method: 'GET',
                concurrency: 10,
                totalRequests: 100
            },
            {
                name: 'Get Pending Requests',
                url: `${this.baseUrl}/borrow-requests/pending-review?page=0&size=20`,
                method: 'GET',
                concurrency: 10,
                totalRequests: 100
            }
        ];

        const allResults = [];

        for (const test of tests) {
            try {
                const result = await this.testEndpointLoad(
                    test.name,
                    test.url,
                    { method: test.method },
                    test.concurrency,
                    test.totalRequests
                );
                allResults.push(result);

                // Nghỉ giữa các test
                await new Promise(resolve => setTimeout(resolve, 3000));
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
     * Test stress với concurrency cao
     */
    async runStressTest() {

        const stressTests = [
            {
                name: 'Stress Test - Get All Books',
                url: `${this.baseUrl}/books?page=0&size=20`,
                concurrencyLevels: [50, 100, 200, 500],
                requestsPerLevel: 1000
            },
            {
                name: 'Stress Test - Search Books',
                url: `${this.baseUrl}/books/search?keyword=java&page=0&size=20`,
                concurrencyLevels: [30, 50, 100, 200],
                requestsPerLevel: 500
            }
        ];

        const allResults = [];

        for (const test of stressTests) {
            try {
                const results = await this.testConcurrencyLevels(
                    test.name,
                    test.url,
                    { method: 'GET' },
                    test.concurrencyLevels,
                    test.requestsPerLevel
                );
                allResults.push(...results);

                // Nghỉ lâu hơn giữa các stress test
                await new Promise(resolve => setTimeout(resolve, 10000));
            } catch (error) {
                console.error(`❌ Lỗi stress test ${test.name}:`, error.message);
            }
        }

        return allResults;
    }

    /**
     * Test endurance với thời gian dài
     */
    async runEnduranceTest(durationMinutes = 5) {

        const startTime = Date.now();
        const endTime = startTime + (durationMinutes * 60 * 1000);
        const results = [];
        let requestCount = 0;

        const testUrl = `${this.baseUrl}/books?page=0&size=20`;
        const concurrency = 10;


        while (Date.now() < endTime) {
            try {
                const batchResults = await this.createConcurrentRequests(
                    testUrl,
                    { method: 'GET' },
                    concurrency,
                    50 // 50 requests per batch
                );

                results.push(...batchResults);
                requestCount += batchResults.length;

                const elapsed = (Date.now() - startTime) / 1000 / 60; // minutes
                const remaining = durationMinutes - elapsed;

                process.stdout.write(`\r   ⏰ Elapsed: ${elapsed.toFixed(1)}min, Remaining: ${remaining.toFixed(1)}min, Requests: ${requestCount}`);

                // Nghỉ ngắn giữa các batch
                await new Promise(resolve => setTimeout(resolve, 1000));
            } catch (error) {
                console.error(`\n❌ Lỗi endurance test:`, error.message);
                break;
            }
        }

        // New line after progress

        const totalDuration = (Date.now() - startTime) / 1000;
        const successfulRequests = results.filter(r => r.success).length;
        const successRate = (successfulRequests / results.length) * 100;
        const requestsPerSecond = results.length / totalDuration;

        const summary = {
            name: 'Endurance Test',
            duration: durationMinutes,
            totalRequests: requestCount,
            successfulRequests,
            successRate: Math.round(successRate * 100) / 100,
            requestsPerSecond: Math.round(requestsPerSecond * 100) / 100,
            totalDuration: Math.round(totalDuration * 100) / 100,
            results
        };


        return summary;
    }

    /**
     * Tạo báo cáo tổng hợp
     */
    generateReport(allResults) {

        // Lọc kết quả hợp lệ
        const validResults = allResults.filter(r => r.successRate !== undefined);

        if (validResults.length === 0) {
            return { summary: {}, details: allResults };
        }

        // Sắp xếp theo success rate
        const sortedBySuccess = [...validResults].sort((a, b) => b.successRate - a.successRate);

        // Sắp xếp theo requests per second
        const sortedByRPS = [...validResults].sort((a, b) => b.requestsPerSecond - a.requestsPerSecond);

        sortedBySuccess.slice(0, 5).forEach((result, index) => {
        });

        sortedByRPS.slice(0, 5).forEach((result, index) => {
        });

        // Thống kê tổng quan
        const totalTests = validResults.length;
        const avgSuccessRate = validResults.reduce((sum, r) => sum + r.successRate, 0) / totalTests;
        const avgRPS = validResults.reduce((sum, r) => sum + r.requestsPerSecond, 0) / totalTests;
        const maxRPS = Math.max(...validResults.map(r => r.requestsPerSecond));
        const minRPS = Math.min(...validResults.map(r => r.requestsPerSecond));


        // Phân loại theo performance
        const excellentTests = validResults.filter(r => r.successRate >= 99 && r.requestsPerSecond >= 100).length;
        const goodTests = validResults.filter(r => r.successRate >= 95 && r.requestsPerSecond >= 50).length;
        const poorTests = validResults.filter(r => r.successRate < 95 || r.requestsPerSecond < 50).length;


        return {
            summary: {
                totalTests,
                avgSuccessRate,
                avgRPS,
                maxRPS,
                minRPS,
                excellentTests,
                goodTests,
                poorTests
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
            testType: 'Load Testing',
            results: results
        };

        fs.writeFileSync(filename, JSON.stringify(data, null, 2));
    }
}

// Hàm chính để chạy load testing
async function main() {
    const tester = new LoadTester();

    // Đăng nhập
    const loginSuccess = await tester.login();
    if (!loginSuccess) {
        console.error('❌ Không thể đăng nhập. Vui lòng kiểm tra thông tin đăng nhập.');
        return;
    }

    const args = process.argv.slice(2);
    const testType = args[0] || 'all';
    const duration = parseInt(args[1]) || 5; // minutes for endurance test

    let allResults = [];

    switch (testType) {
        case 'all':
            allResults = await tester.runAllLoadTests();
            break;

        case 'stress':
            allResults = await tester.runStressTest();
            break;

        case 'endurance':
            const enduranceResult = await tester.runEnduranceTest(duration);
            allResults = [enduranceResult];
            break;

        default:
            return;
    }

    // Tạo báo cáo
    const report = tester.generateReport(allResults);

    // Lưu kết quả
    const filename = `load-test-results-${testType}-${Date.now()}.json`;
    tester.saveResults(report, filename);
}

// Chạy nếu file được gọi trực tiếp
if (require.main === module) {
    main().catch(console.error);
}

module.exports = LoadTester;
