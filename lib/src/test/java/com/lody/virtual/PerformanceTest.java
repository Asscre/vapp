package com.lody.virtual;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.optimization.MemoryOptimizer;
import com.lody.virtual.optimization.NetworkOptimizer;
import com.lody.virtual.optimization.PerformanceMonitor;
import com.lody.virtual.optimization.StartupOptimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 虚拟空间性能测试
 * 测试启动时间、内存使用、并发性能等
 */
public class PerformanceTest extends AndroidTestCase {
    
    private static final String TAG = "PerformanceTest";
    
    private Context mContext;
    private VirtualCore mVirtualCore;
    private StartupOptimizer mStartupOptimizer;
    private MemoryOptimizer mMemoryOptimizer;
    private NetworkOptimizer mNetworkOptimizer;
    private PerformanceMonitor mPerformanceMonitor;
    
    // 测试配置
    private static final String TEST_PACKAGE_NAME = "com.example.performance";
    private static final int TEST_ITERATIONS = 10;
    private static final int CONCURRENT_THREADS = 5;
    private static final long MEMORY_LIMIT = 100 * 1024 * 1024; // 100MB
    private static final int STARTUP_TIMEOUT = 10000; // 10秒
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Log.d(TAG, "Setting up PerformanceTest...");
        
        mContext = getContext();
        mVirtualCore = VirtualCore.get();
        
        // 初始化优化器
        mStartupOptimizer = StartupOptimizer.getInstance();
        mMemoryOptimizer = MemoryOptimizer.getInstance();
        mNetworkOptimizer = NetworkOptimizer.getInstance();
        mPerformanceMonitor = PerformanceMonitor.getInstance();
        
        Log.d(TAG, "PerformanceTest setup completed");
    }
    
    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG, "Tearing down PerformanceTest...");
        
        // 清理测试数据
        cleanupPerformanceData();
        
        super.tearDown();
    }
    
    /**
     * 测试启动时间性能
     */
    public void testStartupPerformance() {
        Log.d(TAG, "Testing startup performance...");
        
        List<Long> startupTimes = new ArrayList<>();
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            Log.d(TAG, "Startup test iteration " + (i + 1) + "/" + TEST_ITERATIONS);
            
            long startTime = System.currentTimeMillis();
            
            try {
                // 测试快速启动策略
                StartupOptimizer.StartupInfo startupInfo = mStartupOptimizer.optimizeStartup(
                    TEST_PACKAGE_NAME, StartupOptimizer.STRATEGY_FAST);
                
                assertNotNull("Startup info should not be null", startupInfo);
                assertTrue("Startup should be successful", startupInfo.success);
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                startupTimes.add(duration);
                
                Log.d(TAG, "Startup iteration " + (i + 1) + " completed in " + duration + "ms");
                
                // 验证启动时间在合理范围内
                assertTrue("Startup time should be reasonable", duration < STARTUP_TIMEOUT);
                
            } catch (Exception e) {
                Log.e(TAG, "Startup test iteration " + (i + 1) + " failed", e);
                fail("Startup should not throw exception: " + e.getMessage());
            }
        }
        
        // 计算统计信息
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;
        
        for (Long time : startupTimes) {
            totalTime += time;
            minTime = Math.min(minTime, time);
            maxTime = Math.max(maxTime, time);
        }
        
        long averageTime = totalTime / startupTimes.size();
        
        Log.d(TAG, "Startup performance results:");
        Log.d(TAG, "  Average time: " + averageTime + "ms");
        Log.d(TAG, "  Min time: " + minTime + "ms");
        Log.d(TAG, "  Max time: " + maxTime + "ms");
        Log.d(TAG, "  Total iterations: " + startupTimes.size());
        
        // 验证性能指标
        assertTrue("Average startup time should be reasonable", averageTime < 5000); // 5秒
        assertTrue("Min startup time should be reasonable", minTime < 3000); // 3秒
        assertTrue("Max startup time should be reasonable", maxTime < 10000); // 10秒
        
        Log.d(TAG, "Startup performance test passed");
    }
    
    /**
     * 测试内存使用性能
     */
    public void testMemoryPerformance() {
        Log.d(TAG, "Testing memory performance...");
        
        try {
            // 设置内存限制
            boolean setLimitResult = mMemoryOptimizer.setMemoryLimit(TEST_PACKAGE_NAME, MEMORY_LIMIT);
            assertTrue("Memory limit should be set successfully", setLimitResult);
            
            // 获取内存信息
            MemoryOptimizer.MemoryInfo memoryInfo = mMemoryOptimizer.getMemoryInfo(TEST_PACKAGE_NAME);
            assertNotNull("Memory info should not be null", memoryInfo);
            assertEquals("Memory limit should match", MEMORY_LIMIT, memoryInfo.memoryLimit);
            
            // 检查内存使用情况
            MemoryOptimizer.MemoryCheckResult checkResult = mMemoryOptimizer.checkMemoryUsage(TEST_PACKAGE_NAME);
            assertNotNull("Memory check result should not be null", checkResult);
            
            Log.d(TAG, "Memory usage: " + checkResult.message);
            
            // 创建对象池测试
            MemoryOptimizer.ObjectPool objectPool = mMemoryOptimizer.createObjectPool(
                TEST_PACKAGE_NAME, "test_pool", 100);
            assertNotNull("Object pool should be created", objectPool);
            
            // 创建缓存管理器测试
            MemoryOptimizer.CacheManager cacheManager = mMemoryOptimizer.createCacheManager(
                TEST_PACKAGE_NAME, "test_cache", 50, MemoryOptimizer.CACHE_POLICY_LRU);
            assertNotNull("Cache manager should be created", cacheManager);
            
            // 测试内存优化
            MemoryOptimizer.MemoryOptimizationResult optimizationResult = 
                mMemoryOptimizer.optimizeMemory(TEST_PACKAGE_NAME);
            assertNotNull("Memory optimization result should not be null", optimizationResult);
            
            Log.d(TAG, "Memory optimization: " + optimizationResult.message);
            
            // 获取内存统计信息
            MemoryOptimizer.MemoryStatistics statistics = mMemoryOptimizer.getMemoryStatistics();
            assertNotNull("Memory statistics should not be null", statistics);
            
            Log.d(TAG, "Memory statistics:");
            Log.d(TAG, "  Total apps: " + statistics.totalApps);
            Log.d(TAG, "  Total memory limit: " + statistics.totalMemoryLimit + " bytes");
            Log.d(TAG, "  Total memory usage: " + statistics.totalMemoryUsage + " bytes");
            Log.d(TAG, "  Over limit apps: " + statistics.overLimitApps);
            
            Log.d(TAG, "Memory performance test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Memory performance test failed", e);
            fail("Memory performance should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试网络性能
     */
    public void testNetworkPerformance() {
        Log.d(TAG, "Testing network performance...");
        
        try {
            // 检查网络连接
            NetworkOptimizer.NetworkCheckResult networkCheck = mNetworkOptimizer.checkNetworkConnection();
            assertNotNull("Network check result should not be null", networkCheck);
            
            if (!networkCheck.isConnected) {
                Log.w(TAG, "No network connection, skipping network performance test");
                return;
            }
            
            // 设置网络配置
            NetworkOptimizer.NetworkConfig config = new NetworkOptimizer.NetworkConfig();
            config.connectionTimeout = 5000;
            config.readTimeout = 15000;
            config.maxConnections = 20;
            config.cacheSize = 200;
            
            boolean setConfigResult = mNetworkOptimizer.setNetworkConfig(TEST_PACKAGE_NAME, config);
            assertTrue("Network config should be set successfully", setConfigResult);
            
            // 测试网络请求优化
            String testUrl = "https://httpbin.org/get";
            NetworkOptimizer.NetworkRequestResult requestResult = mNetworkOptimizer.optimizeRequest(
                TEST_PACKAGE_NAME, testUrl, "GET", null, null, NetworkOptimizer.STRATEGY_BALANCED);
            
            assertNotNull("Network request result should not be null", requestResult);
            
            Log.d(TAG, "Network request: " + requestResult.message);
            
            // 测试批量请求
            List<NetworkOptimizer.NetworkRequest> requests = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                requests.add(new NetworkOptimizer.NetworkRequest(
                    testUrl + "?id=" + i, "GET", null, null));
            }
            
            NetworkOptimizer.BatchRequestResult batchResult = mNetworkOptimizer.optimizeBatchRequests(
                TEST_PACKAGE_NAME, requests, NetworkOptimizer.STRATEGY_BALANCED);
            
            assertNotNull("Batch request result should not be null", batchResult);
            assertTrue("Batch request should be successful", batchResult.success);
            
            Log.d(TAG, "Batch request: " + batchResult.message);
            
            // 获取网络统计信息
            NetworkOptimizer.NetworkStatistics statistics = mNetworkOptimizer.getNetworkStatistics();
            assertNotNull("Network statistics should not be null", statistics);
            
            Log.d(TAG, "Network statistics:");
            Log.d(TAG, "  Total apps: " + statistics.totalApps);
            Log.d(TAG, "  Total requests: " + statistics.totalRequests);
            Log.d(TAG, "  Total bytes: " + statistics.totalBytes);
            Log.d(TAG, "  Average response time: " + statistics.averageResponseTime + "ms");
            
            Log.d(TAG, "Network performance test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Network performance test failed", e);
            fail("Network performance should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试并发性能
     */
    public void testConcurrentPerformance() {
        Log.d(TAG, "Testing concurrent performance...");
        
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    Log.d(TAG, "Concurrent test thread " + threadId + " started");
                    
                    // 模拟并发操作
                    String packageName = TEST_PACKAGE_NAME + "_" + threadId;
                    
                    // 测试启动优化
                    StartupOptimizer.StartupInfo startupInfo = mStartupOptimizer.optimizeStartup(
                        packageName, StartupOptimizer.STRATEGY_BALANCED);
                    assertNotNull("Startup info should not be null", startupInfo);
                    
                    // 测试内存优化
                    mMemoryOptimizer.setMemoryLimit(packageName, MEMORY_LIMIT);
                    MemoryOptimizer.MemoryInfo memoryInfo = mMemoryOptimizer.getMemoryInfo(packageName);
                    assertNotNull("Memory info should not be null", memoryInfo);
                    
                    // 测试网络优化
                    NetworkOptimizer.NetworkCheckResult networkCheck = mNetworkOptimizer.checkNetworkConnection();
                    assertNotNull("Network check result should not be null", networkCheck);
                    
                    Log.d(TAG, "Concurrent test thread " + threadId + " completed");
                    
                } catch (Exception e) {
                    Log.e(TAG, "Concurrent test thread " + threadId + " failed", e);
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                } finally {
                    latch.countDown();
                }
            });
            
            threads.add(thread);
            thread.start();
        }
        
        try {
            // 等待所有线程完成
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assertTrue("All threads should complete within timeout", completed);
            
            // 检查是否有异常
            assertTrue("No exceptions should occur in concurrent test", exceptions.isEmpty());
            
            Log.d(TAG, "Concurrent performance test passed with " + CONCURRENT_THREADS + " threads");
            
        } catch (InterruptedException e) {
            Log.e(TAG, "Concurrent test interrupted", e);
            fail("Concurrent test should not be interrupted: " + e.getMessage());
        }
    }
    
    /**
     * 测试性能监控
     */
    public void testPerformanceMonitoring() {
        Log.d(TAG, "Testing performance monitoring...");
        
        try {
            // 启动性能监控
            List<Integer> monitorTypes = new ArrayList<>();
            monitorTypes.add(PerformanceMonitor.MONITOR_TYPE_CPU);
            monitorTypes.add(PerformanceMonitor.MONITOR_TYPE_MEMORY);
            monitorTypes.add(PerformanceMonitor.MONITOR_TYPE_NETWORK);
            
            boolean startResult = mPerformanceMonitor.startMonitoring(TEST_PACKAGE_NAME, monitorTypes);
            assertTrue("Performance monitoring should start successfully", startResult);
            
            // 记录性能数据
            boolean recordResult = mPerformanceMonitor.recordPerformanceData(
                TEST_PACKAGE_NAME, PerformanceMonitor.MONITOR_TYPE_CPU, 50.0, "%");
            assertTrue("Performance data should be recorded successfully", recordResult);
            
            // 添加告警规则
            boolean alertResult = mPerformanceMonitor.addAlertRule(
                TEST_PACKAGE_NAME, PerformanceMonitor.MONITOR_TYPE_CPU, 80.0, PerformanceMonitor.ALERT_LEVEL_WARNING);
            assertTrue("Alert rule should be added successfully", alertResult);
            
            // 获取性能数据
            PerformanceMonitor.PerformanceData performanceData = mPerformanceMonitor.getPerformanceData(TEST_PACKAGE_NAME);
            assertNotNull("Performance data should not be null", performanceData);
            assertTrue("Performance monitoring should be active", performanceData.isMonitoring);
            
            // 生成性能报告
            long startTime = System.currentTimeMillis() - 60000; // 1分钟前
            long endTime = System.currentTimeMillis();
            
            PerformanceMonitor.PerformanceReport report = mPerformanceMonitor.generateReport(
                TEST_PACKAGE_NAME, startTime, endTime);
            assertNotNull("Performance report should not be null", report);
            
            Log.d(TAG, "Performance report generated with " + report.records.size() + " records");
            
            // 获取性能统计信息
            PerformanceMonitor.PerformanceStatistics statistics = mPerformanceMonitor.getPerformanceStatistics();
            assertNotNull("Performance statistics should not be null", statistics);
            
            Log.d(TAG, "Performance statistics:");
            Log.d(TAG, "  Total apps: " + statistics.totalApps);
            Log.d(TAG, "  Active apps: " + statistics.activeApps);
            Log.d(TAG, "  Total records: " + statistics.totalRecords);
            Log.d(TAG, "  Total alerts: " + statistics.totalAlerts);
            
            // 停止性能监控
            boolean stopResult = mPerformanceMonitor.stopMonitoring(TEST_PACKAGE_NAME);
            assertTrue("Performance monitoring should stop successfully", stopResult);
            
            Log.d(TAG, "Performance monitoring test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Performance monitoring test failed", e);
            fail("Performance monitoring should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试压力测试
     */
    public void testStressTest() {
        Log.d(TAG, "Testing stress test...");
        
        int stressIterations = 50;
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < stressIterations; i++) {
            Log.d(TAG, "Stress test iteration " + (i + 1) + "/" + stressIterations);
            
            long startTime = System.currentTimeMillis();
            
            try {
                // 模拟压力操作
                String packageName = TEST_PACKAGE_NAME + "_stress_" + i;
                
                // 启动优化
                StartupOptimizer.StartupInfo startupInfo = mStartupOptimizer.optimizeStartup(
                    packageName, StartupOptimizer.STRATEGY_FAST);
                assertNotNull("Startup info should not be null", startupInfo);
                
                // 内存优化
                mMemoryOptimizer.setMemoryLimit(packageName, MEMORY_LIMIT);
                MemoryOptimizer.MemoryInfo memoryInfo = mMemoryOptimizer.getMemoryInfo(packageName);
                assertNotNull("Memory info should not be null", memoryInfo);
                
                // 网络检查
                NetworkOptimizer.NetworkCheckResult networkCheck = mNetworkOptimizer.checkNetworkConnection();
                assertNotNull("Network check result should not be null", networkCheck);
                
                // 性能监控
                mPerformanceMonitor.recordPerformanceData(
                    packageName, PerformanceMonitor.MONITOR_TYPE_CPU, Math.random() * 100, "%");
                
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                responseTimes.add(responseTime);
                
                Log.d(TAG, "Stress test iteration " + (i + 1) + " completed in " + responseTime + "ms");
                
            } catch (Exception e) {
                Log.e(TAG, "Stress test iteration " + (i + 1) + " failed", e);
                fail("Stress test should not throw exception: " + e.getMessage());
            }
        }
        
        // 计算压力测试统计信息
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;
        
        for (Long time : responseTimes) {
            totalTime += time;
            minTime = Math.min(minTime, time);
            maxTime = Math.max(maxTime, time);
        }
        
        long averageTime = totalTime / responseTimes.size();
        
        Log.d(TAG, "Stress test results:");
        Log.d(TAG, "  Total iterations: " + responseTimes.size());
        Log.d(TAG, "  Average response time: " + averageTime + "ms");
        Log.d(TAG, "  Min response time: " + minTime + "ms");
        Log.d(TAG, "  Max response time: " + maxTime + "ms");
        
        // 验证压力测试结果
        assertTrue("Average response time should be reasonable", averageTime < 1000); // 1秒
        assertTrue("Max response time should be reasonable", maxTime < 5000); // 5秒
        
        Log.d(TAG, "Stress test passed");
    }
    
    /**
     * 清理性能测试数据
     */
    private void cleanupPerformanceData() {
        Log.d(TAG, "Cleaning up performance test data...");
        
        try {
            // 清理启动优化数据
            mStartupOptimizer.clearStartupCache(TEST_PACKAGE_NAME);
            
            // 清理内存优化数据
            mMemoryOptimizer.cleanupMemory(TEST_PACKAGE_NAME);
            
            // 清理网络优化数据
            mNetworkOptimizer.cleanupNetworkCache(TEST_PACKAGE_NAME);
            
            // 清理性能监控数据
            mPerformanceMonitor.cleanupPerformanceData(TEST_PACKAGE_NAME);
            
            Log.d(TAG, "Performance test data cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup performance test data", e);
        }
    }
} 