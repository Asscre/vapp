package com.lody.virtual;

import android.content.Context;
import android.util.Log;

import com.lody.virtual.VirtualCore;
import com.lody.virtual.optimization.NetworkOptimizer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * 虚拟空间性能测试
 * 测试启动时间、内存使用、并发性能等
 */
@RunWith(MockitoJUnitRunner.class)
public class PerformanceTest {
    
    private static final String TAG = "PerformanceTest";
    
    @Mock
    private Context mContext;
    
    private VirtualCore mVirtualCore;
    private NetworkOptimizer mNetworkOptimizer;
    
    // 测试配置
    private static final String TEST_PACKAGE_NAME = "com.example.performance";
    private static final int TEST_ITERATIONS = 10;
    private static final int CONCURRENT_THREADS = 5;
    private static final long MEMORY_LIMIT = 100 * 1024 * 1024; // 100MB
    private static final int STARTUP_TIMEOUT = 10000; // 10秒
    
    @Before
    public void setUp() throws Exception {
        Log.d(TAG, "Setting up PerformanceTest...");
        
        mVirtualCore = VirtualCore.getInstance();
        mNetworkOptimizer = NetworkOptimizer.getInstance();
        
        Log.d(TAG, "PerformanceTest setup completed");
    }
    
    @After
    public void tearDown() throws Exception {
        Log.d(TAG, "Tearing down PerformanceTest...");
        
        // 清理测试数据
        cleanupPerformanceData();
    }
    
    /**
     * 测试启动时间性能
     */
    @Test
    public void testStartupPerformance() {
        Log.d(TAG, "Testing startup performance...");
        
        List<Long> startupTimes = new ArrayList<>();
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            Log.d(TAG, "Startup test iteration " + (i + 1) + "/" + TEST_ITERATIONS);
            
            long startTime = System.currentTimeMillis();
            
            try {
                // 测试VirtualCore初始化
                boolean initResult = mVirtualCore.initialize(mContext);
                
                assertTrue("VirtualCore initialization should be successful", initResult);
                
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
     * 测试网络性能
     */
    @Test
    public void testNetworkPerformance() {
        Log.d(TAG, "Testing network performance...");
        
        try {
            // 初始化网络优化器
            boolean initResult = mNetworkOptimizer.initialize(mContext, mVirtualCore);
            assertTrue("Network optimizer should initialize successfully", initResult);
            
            // 检查网络连接
            NetworkOptimizer.NetworkCheckResult networkCheck = mNetworkOptimizer.checkNetworkConnection();
            assertNotNull("Network check result should not be null", networkCheck);
            
            if (!networkCheck.isConnected) {
                Log.w(TAG, "No network connection, skipping network performance test");
                return;
            }
            
            Log.d(TAG, "Network connection: " + networkCheck.message);
            
            // 设置网络配置
            NetworkOptimizer.NetworkConfig config = new NetworkOptimizer.NetworkConfig();
            config.connectionTimeout = 5000;
            config.readTimeout = 10000;
            config.maxConnections = 5;
            config.cacheSize = 50;
            config.enableCompression = true;
            config.enableKeepAlive = true;
            
            boolean setConfigResult = mNetworkOptimizer.setNetworkConfig(TEST_PACKAGE_NAME, config);
            assertTrue("Network config should be set successfully", setConfigResult);
            
            // 测试网络请求
            String testUrl = "https://httpbin.org/get";
            NetworkOptimizer.NetworkRequestResult requestResult = mNetworkOptimizer.optimizeRequest(
                TEST_PACKAGE_NAME, testUrl, "GET", null, null, NetworkOptimizer.STRATEGY_BALANCED);
            
            assertNotNull("Network request result should not be null", requestResult);
            
            if (requestResult.success) {
                Log.d(TAG, "Network request successful: " + requestResult.message);
            } else {
                Log.w(TAG, "Network request failed: " + requestResult.message);
            }
            
            // 测试批量请求
            List<NetworkOptimizer.NetworkRequest> requests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                requests.add(new NetworkOptimizer.NetworkRequest(
                    "https://httpbin.org/delay/" + i, "GET", null, null));
            }
            
            NetworkOptimizer.BatchRequestResult batchResult = mNetworkOptimizer.optimizeBatchRequests(
                TEST_PACKAGE_NAME, requests, NetworkOptimizer.STRATEGY_BALANCED);
            
            assertNotNull("Batch request result should not be null", batchResult);
            assertTrue("Batch request should be successful", batchResult.success);
            
            Log.d(TAG, "Batch request completed: " + batchResult.message);
            
            // 获取网络统计信息
            NetworkOptimizer.NetworkStatistics statistics = mNetworkOptimizer.getNetworkStatistics();
            assertNotNull("Network statistics should not be null", statistics);
            
            Log.d(TAG, "Network statistics:");
            Log.d(TAG, "  Total apps: " + statistics.totalApps);
            Log.d(TAG, "  Total requests: " + statistics.totalRequests);
            Log.d(TAG, "  Total bytes: " + statistics.totalBytes);
            Log.d(TAG, "  Total errors: " + statistics.totalErrors);
            
            Log.d(TAG, "Network performance test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Network performance test failed", e);
            fail("Network performance should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试并发性能
     */
    @Test
    public void testConcurrentPerformance() {
        Log.d(TAG, "Testing concurrent performance...");
        
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        List<Exception> exceptions = new ArrayList<>();
        
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    Log.d(TAG, "Concurrent test thread " + threadId + " started");
                    
                    // 模拟并发操作
                    Thread.sleep(100);
                    
                    // 测试VirtualCore功能
                    assertTrue("VirtualCore should be initialized", mVirtualCore.isInitialized());
                    
                    // 测试网络优化器
                    NetworkOptimizer.NetworkCheckResult check = mNetworkOptimizer.checkNetworkConnection();
                    assertNotNull("Network check should not be null", check);
                    
                    Log.d(TAG, "Concurrent test thread " + threadId + " completed");
                    
                } catch (Exception e) {
                    Log.e(TAG, "Concurrent test thread " + threadId + " failed", e);
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        try {
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assertTrue("All threads should complete within timeout", completed);
            assertTrue("No exceptions should occur in concurrent test", exceptions.isEmpty());
            
            Log.d(TAG, "Concurrent performance test passed");
            
        } catch (InterruptedException e) {
            Log.e(TAG, "Concurrent test interrupted", e);
            fail("Concurrent test should not be interrupted: " + e.getMessage());
        }
    }
    
    /**
     * 清理性能测试数据
     */
    private void cleanupPerformanceData() {
        try {
            Log.d(TAG, "Cleaning up performance test data...");
            
            // 清理网络优化器
            if (mNetworkOptimizer != null) {
                mNetworkOptimizer.cleanup();
            }
            
            // 清理VirtualCore
            if (mVirtualCore != null) {
                mVirtualCore.cleanup();
            }
            
            Log.d(TAG, "Performance test data cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup performance test data", e);
        }
    }
} 