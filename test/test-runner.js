/**
 * Test Runner chính cho hệ thống quản lý thư viện
 * Chạy tất cả các loại test: Mock Data, Performance, Load, Database
 */

const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

class TestRunner {
    constructor() {
        this.results = {
            mockData: null,
            performance: null,
            load: null,
            database: null
        };
        this.startTime = null;
        this.endTime = null;
    }

    /**
     * Chạy một script con và trả về kết quả
     */
    async runScript(scriptPath, args = []) {
        return new Promise((resolve, reject) => {
            console.log(`\n🚀 Chạy script: ${scriptPath} ${args.join(' ')}`);

            const child = spawn('node', [scriptPath, ...args], {
                stdio: 'inherit',
                cwd: process.cwd()
            });

            child.on('close', (code) => {
                if (code === 0) {
                    console.log(`✅ Script ${scriptPath} hoàn thành thành công`);
                    resolve({ success: true, code });
                } else {
                    console.log(`❌ Script ${scriptPath} thất bại với code ${code}`);
                    resolve({ success: false, code });
                }
            });

            child.on('error', (error) => {
                console.error(`❌ Lỗi chạy script ${scriptPath}:`, error.message);
                reject(error);
            });
        });
    }

    /**
     * Chạy Mock Data Generator
     */
    async runMockDataGenerator(bookCount = '1000') {
        console.log('\n' + '='.repeat(80));
        console.log('📚 MOCK DATA GENERATOR');
        console.log('='.repeat(80));

        try {
            const result = await this.runScript('mock-data-generator.js', [bookCount]);
            this.results.mockData = {
                success: result.success,
                bookCount: bookCount,
                timestamp: new Date().toISOString()
            };
            return result.success;
        } catch (error) {
            console.error('❌ Lỗi chạy Mock Data Generator:', error.message);
            this.results.mockData = {
                success: false,
                error: error.message,
                timestamp: new Date().toISOString()
            };
            return false;
        }
    }

    /**
     * Chạy Performance Tester
     */
    async runPerformanceTester(testType = 'all') {
        console.log('\n' + '='.repeat(80));
        console.log('⚡ PERFORMANCE TESTER');
        console.log('='.repeat(80));

        try {
            const result = await this.runScript('performance-tester.js', [testType]);
            this.results.performance = {
                success: result.success,
                testType: testType,
                timestamp: new Date().toISOString()
            };
            return result.success;
        } catch (error) {
            console.error('❌ Lỗi chạy Performance Tester:', error.message);
            this.results.performance = {
                success: false,
                error: error.message,
                timestamp: new Date().toISOString()
            };
            return false;
        }
    }

    /**
     * Chạy Load Tester
     */
    async runLoadTester(testType = 'all', duration = 5) {
        console.log('\n' + '='.repeat(80));
        console.log('🔥 LOAD TESTER');
        console.log('='.repeat(80));

        try {
            const args = [testType];
            if (testType === 'endurance') {
                args.push(duration.toString());
            }

            const result = await this.runScript('load-tester.js', args);
            this.results.load = {
                success: result.success,
                testType: testType,
                duration: testType === 'endurance' ? duration : null,
                timestamp: new Date().toISOString()
            };
            return result.success;
        } catch (error) {
            console.error('❌ Lỗi chạy Load Tester:', error.message);
            this.results.load = {
                success: false,
                error: error.message,
                timestamp: new Date().toISOString()
            };
            return false;
        }
    }

    /**
     * Chạy Database Tester
     */
    async runDatabaseTester(testType = 'all') {
        console.log('\n' + '='.repeat(80));
        console.log('🗄️  DATABASE TESTER');
        console.log('='.repeat(80));

        try {
            const result = await this.runScript('database-tester.js', [testType]);
            this.results.database = {
                success: result.success,
                testType: testType,
                timestamp: new Date().toISOString()
            };
            return result.success;
        } catch (error) {
            console.error('❌ Lỗi chạy Database Tester:', error.message);
            this.results.database = {
                success: false,
                error: error.message,
                timestamp: new Date().toISOString()
            };
            return false;
        }
    }

    /**
     * Chạy tất cả các test theo thứ tự
     */
    async runAllTests(options = {}) {
        this.startTime = Date.now();

        console.log('🎯 BẮT ĐẦU CHẠY TẤT CẢ CÁC TEST');
        console.log('='.repeat(80));
        console.log(`📅 Thời gian bắt đầu: ${new Date().toLocaleString()}`);
        console.log(`⚙️  Cấu hình:`);
        console.log(`   - Mock Data: ${options.mockData || '1000'} quyển sách`);
        console.log(`   - Performance Test: ${options.performance || 'all'}`);
        console.log(`   - Load Test: ${options.load || 'all'}`);
        console.log(`   - Database Test: ${options.database || 'all'}`);
        console.log('='.repeat(80));

        const results = {
            mockData: false,
            performance: false,
            load: false,
            database: false
        };

        // 1. Chạy Mock Data Generator (nếu được yêu cầu)
        if (options.runMockData !== false) {
            results.mockData = await this.runMockDataGenerator(options.mockData || '1000');

            if (!results.mockData) {
                console.log('⚠️  Mock Data Generator thất bại, nhưng vẫn tiếp tục với các test khác...');
            }

            // Nghỉ 5 giây sau khi tạo mock data
            console.log('⏳ Nghỉ 5 giây để hệ thống ổn định...');
            await new Promise(resolve => setTimeout(resolve, 5000));
        }

        // 2. Chạy Performance Tester
        if (options.runPerformance !== false) {
            results.performance = await this.runPerformanceTester(options.performance || 'all');

            // Nghỉ 3 giây giữa các test
            console.log('⏳ Nghỉ 3 giây giữa các test...');
            await new Promise(resolve => setTimeout(resolve, 3000));
        }

        // 3. Chạy Load Tester
        if (options.runLoad !== false) {
            results.load = await this.runLoadTester(options.load || 'all', options.loadDuration || 5);

            // Nghỉ 3 giây giữa các test
            console.log('⏳ Nghỉ 3 giây giữa các test...');
            await new Promise(resolve => setTimeout(resolve, 3000));
        }

        // 4. Chạy Database Tester
        if (options.runDatabase !== false) {
            results.database = await this.runDatabaseTester(options.database || 'all');
        }

        this.endTime = Date.now();

        // Tạo báo cáo tổng hợp
        this.generateFinalReport(results);

        return results;
    }

    /**
     * Chạy test theo từng loại riêng biệt
     */
    async runSpecificTest(testType, options = {}) {
        this.startTime = Date.now();

        console.log(`🎯 CHẠY TEST: ${testType.toUpperCase()}`);
        console.log('='.repeat(80));

        let success = false;

        switch (testType.toLowerCase()) {
            case 'mock':
            case 'mockdata':
                success = await this.runMockDataGenerator(options.bookCount || '1000');
                break;

            case 'performance':
            case 'perf':
                success = await this.runPerformanceTester(options.testType || 'all');
                break;

            case 'load':
                success = await this.runLoadTester(options.testType || 'all', options.duration || 5);
                break;

            case 'database':
            case 'db':
                success = await this.runDatabaseTester(options.testType || 'all');
                break;

            default:
                console.error(`❌ Loại test không hợp lệ: ${testType}`);
                console.log('Các loại test hợp lệ: mock, performance, load, database');
                return false;
        }

        this.endTime = Date.now();

        console.log(`\n${success ? '✅' : '❌'} Test ${testType} ${success ? 'thành công' : 'thất bại'}`);
        console.log(`⏱️  Thời gian thực hiện: ${((this.endTime - this.startTime) / 1000).toFixed(2)} giây`);

        return success;
    }

    /**
     * Tạo báo cáo tổng hợp cuối cùng
     */
    generateFinalReport(results) {
        this.endTime = Date.now();
        const totalDuration = (this.endTime - this.startTime) / 1000;

        console.log('\n' + '='.repeat(80));
        console.log('📊 BÁO CÁO TỔNG HỢP CUỐI CÙNG');
        console.log('='.repeat(80));
        console.log(`📅 Thời gian bắt đầu: ${new Date(this.startTime).toLocaleString()}`);
        console.log(`📅 Thời gian kết thúc: ${new Date(this.endTime).toLocaleString()}`);
        console.log(`⏱️  Tổng thời gian: ${totalDuration.toFixed(2)} giây`);
        console.log('');

        // Thống kê kết quả
        const testNames = {
            mockData: 'Mock Data Generator',
            performance: 'Performance Tester',
            load: 'Load Tester',
            database: 'Database Tester'
        };

        let successCount = 0;
        let totalCount = 0;

        console.log('📋 KẾT QUẢ CÁC TEST:');
        for (const [key, name] of Object.entries(testNames)) {
            if (results[key] !== undefined) {
                totalCount++;
                const success = results[key];
                if (success) successCount++;

                console.log(`   ${success ? '✅' : '❌'} ${name}: ${success ? 'THÀNH CÔNG' : 'THẤT BẠI'}`);
            }
        }

        console.log('');
        console.log(`📈 TỔNG KẾT: ${successCount}/${totalCount} tests thành công (${Math.round(successCount / totalCount * 100)}%)`);

        // Đánh giá tổng thể
        if (successCount === totalCount) {
            console.log('🎉 TẤT CẢ TESTS ĐỀU THÀNH CÔNG! Hệ thống hoạt động tốt.');
        } else if (successCount >= totalCount * 0.75) {
            console.log('👍 HẦU HẾT TESTS THÀNH CÔNG! Hệ thống hoạt động khá tốt.');
        } else if (successCount >= totalCount * 0.5) {
            console.log('⚠️  MỘT SỐ TESTS THẤT BẠI! Cần kiểm tra và cải thiện hệ thống.');
        } else {
            console.log('❌ NHIỀU TESTS THẤT BẠI! Hệ thống cần được kiểm tra kỹ lưỡng.');
        }

        // Lưu báo cáo tổng hợp
        this.saveFinalReport(results, totalDuration);
    }

    /**
     * Lưu báo cáo tổng hợp
     */
    saveFinalReport(results, totalDuration) {
        const report = {
            timestamp: new Date().toISOString(),
            startTime: new Date(this.startTime).toISOString(),
            endTime: new Date(this.endTime).toISOString(),
            totalDuration: totalDuration,
            results: results,
            summary: {
                totalTests: Object.keys(results).length,
                successfulTests: Object.values(results).filter(r => r === true).length,
                failedTests: Object.values(results).filter(r => r === false).length
            }
        };

        const filename = `test-runner-report-${Date.now()}.json`;
        fs.writeFileSync(filename, JSON.stringify(report, null, 2));
        console.log(`\n💾 Báo cáo tổng hợp đã được lưu vào ${filename}`);
    }

    /**
     * Hiển thị hướng dẫn sử dụng
     */
    showHelp() {
        console.log(`
🎯 TEST RUNNER - Hệ thống quản lý thư viện

📚 CÁCH SỬ DỤNG:

1. Chạy tất cả tests:
   node test-runner.js all

2. Chạy test cụ thể:
   node test-runner.js mock [số_quyển_sách]
   node test-runner.js performance [loại_test]
   node test-runner.js load [loại_test] [thời_gian_phút]
   node test-runner.js database [loại_test]

3. Chạy với tùy chọn:
   node test-runner.js all --mock-data=10000 --performance=all --load=stress --database=all

📋 CÁC LOẠI TEST:

Mock Data Generator:
   - 1000, 1m, 10m, 100m: Số lượng sách cần tạo

Performance Tester:
   - all: Tất cả performance tests
   - pagination: Test pagination
   - search: Test search performance

Load Tester:
   - all: Tất cả load tests
   - stress: Stress testing
   - endurance: Endurance testing (cần thời gian)

Database Tester:
   - all: Tất cả database tests
   - basic: Basic queries
   - search: Search queries
   - complex: Complex queries với JOINs
   - pagination: Pagination performance
   - index: Index performance
   - analytics: Analytics queries
   - concurrent: Concurrent queries

📁 FILES ĐƯỢC TẠO:
   - mock-data-results-*.json: Kết quả mock data
   - performance-test-results-*.json: Kết quả performance
   - load-test-results-*.json: Kết quả load testing
   - database-test-results-*.json: Kết quả database
   - test-runner-report-*.json: Báo cáo tổng hợp

⚠️  LƯU Ý:
   - Đảm bảo server đang chạy trên localhost:8080
   - Đảm bảo database PostgreSQL đang chạy
   - Cài đặt dependencies: npm install pg
   - Một số test có thể mất rất nhiều thời gian
        `);
    }
}

// Hàm chính
async function main() {
    const args = process.argv.slice(2);

    if (args.length === 0 || args[0] === '--help' || args[0] === '-h') {
        const runner = new TestRunner();
        runner.showHelp();
        return;
    }

    const runner = new TestRunner();
    const command = args[0];

    try {
        if (command === 'all') {
            // Parse options từ command line
            const options = {
                mockData: '1000',
                performance: 'all',
                load: 'all',
                database: 'all',
                loadDuration: 5
            };

            // Parse các tùy chọn
            for (let i = 1; i < args.length; i++) {
                const arg = args[i];
                if (arg.startsWith('--')) {
                    const [key, value] = arg.substring(2).split('=');
                    switch (key) {
                        case 'mock-data':
                            options.mockData = value;
                            break;
                        case 'performance':
                            options.performance = value;
                            break;
                        case 'load':
                            options.load = value;
                            break;
                        case 'database':
                            options.database = value;
                            break;
                        case 'load-duration':
                            options.loadDuration = parseInt(value);
                            break;
                    }
                }
            }

            await runner.runAllTests(options);
        } else {
            // Chạy test cụ thể
            const options = {};

            // Parse arguments cho test cụ thể
            if (command === 'mock' && args[1]) {
                options.bookCount = args[1];
            } else if ((command === 'performance' || command === 'load' || command === 'database') && args[1]) {
                options.testType = args[1];
            }

            if (command === 'load' && args[2]) {
                options.duration = parseInt(args[2]);
            }

            await runner.runSpecificTest(command, options);
        }
    } catch (error) {
        console.error('❌ Lỗi chạy test runner:', error.message);
        process.exit(1);
    }
}

// Chạy nếu file được gọi trực tiếp
if (require.main === module) {
    main().catch(console.error);
}

module.exports = TestRunner;
