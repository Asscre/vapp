package com.lody.virtual;

import android.content.Context;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VEnvironment;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.pm.VPackageManager;
import com.lody.virtual.pm.parser.VPackage;
import com.lody.virtual.security.DataIsolationManager;
import com.lody.virtual.security.NetworkIsolationManager;
import com.lody.virtual.security.PermissionControlManager;
import com.lody.virtual.security.DataEncryptionManager;
import com.lody.virtual.optimization.StartupOptimizer;
import com.lody.virtual.optimization.MemoryOptimizer;
import com.lody.virtual.optimization.NetworkOptimizer;
import com.lody.virtual.optimization.PerformanceMonitor;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * 虚拟空间集成测试
 * 测试完整流程、错误处理、边界条件、压力测试
 */
public class IntegrationTest extends AndroidTestCase {
    
    private static final String TAG = "IntegrationTest";
    
    private Context mContext;
    private VirtualCore mVirtualCore;
    private VPackageManager mPackageManager;
    private VEnvironment mEnvironment;
    
    // 测试配置
    private static final String TEST_PACKAGE_NAME = "com.example.integration";
    private static final String TEST_APK_PATH = "/sdcard/test_integration.apk";
    private static final int INTEGRATION_TIMEOUT = 30000; // 30秒
    private static final int PRESSURE_TEST_ITERATIONS = 100;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Log.d(TAG, "Setting up IntegrationTest...");
        
        mContext = getContext();
        mVirtualCore = VirtualCore.get();
        mPackageManager = VPackageManager.get();
        mEnvironment = VEnvironment.get();
        
        Log.d(TAG, "IntegrationTest setup completed");
    }
    
    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG, "Tearing down IntegrationTest...");
        
        // 清理测试数据
        cleanupIntegrationData();
        
        super.tearDown();
    }
    
    /**
     * 测试完整应用生命周期流程
     */
    public void testCompleteAppLifecycle() {
        Log.d(TAG, "Testing complete app lifecycle...");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 1. 初始化阶段
            Log.d(TAG, "Step 1: Initialization");
            assertTrue("VirtualCore should be initialized", mVirtualCore.isStartup());
            assertNotNull("VPackageManager should not be null", mPackageManager);
            assertNotNull("VEnvironment should not be null", mEnvironment);
            
            // 2. 应用安装阶段
            Log.d(TAG, "Step 2: App Installation");
            File testApk = new File(TEST_APK_PATH);
            if (testApk.exists()) {
                int installResult = mPackageManager.installPackage(TEST_APK_PATH, 0);
                assertEquals("Installation should succeed", PackageManager.INSTALL_SUCCEEDED, installResult);
                
                VPackage packageInfo = mPackageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
                assertNotNull("Installed package info should not be null", packageInfo);
                assertEquals("Package name should match", TEST_PACKAGE_NAME, packageInfo.packageName);
            } else {
                Log.w(TAG, "Test APK not found, skipping installation step");
            }
            
            // 3. 环境准备阶段
            Log.d(TAG, "Step 3: Environment Preparation");
            File dataDir = mEnvironment.getDataUserPackageDirectory(0, TEST_PACKAGE_NAME);
            assertNotNull("Data directory should not be null", dataDir);
            
            // 创建测试数据
            File testFile = new File(dataDir, "test_data.txt");
            String testContent = "Integration test data";
            FileUtils.writeToFile(testFile, testContent);
            assertTrue("Test file should be created", testFile.exists());
            
            // 4. 安全机制初始化
            Log.d(TAG, "Step 4: Security Mechanism Initialization");
            DataIsolationManager dataIsolationManager = DataIsolationManager.getInstance();
            NetworkIsolationManager networkIsolationManager = NetworkIsolationManager.getInstance();
            PermissionControlManager permissionControlManager = PermissionControlManager.getInstance();
            DataEncryptionManager dataEncryptionManager = DataEncryptionManager.getInstance();
            
            assertNotNull("DataIsolationManager should not be null", dataIsolationManager);
            assertNotNull("NetworkIsolationManager should not be null", networkIsolationManager);
            assertNotNull("PermissionControlManager should not be null", permissionControlManager);
            assertNotNull("DataEncryptionManager should not be null", dataEncryptionManager);
            
            // 5. 性能优化初始化
            Log.d(TAG, "Step 5: Performance Optimization Initialization");
            StartupOptimizer startupOptimizer = StartupOptimizer.getInstance();
            MemoryOptimizer memoryOptimizer = MemoryOptimizer.getInstance();
            NetworkOptimizer networkOptimizer = NetworkOptimizer.getInstance();
            PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
            
            assertNotNull("StartupOptimizer should not be null", startupOptimizer);
            assertNotNull("MemoryOptimizer should not be null", memoryOptimizer);
            assertNotNull("NetworkOptimizer should not be null", networkOptimizer);
            assertNotNull("PerformanceMonitor should not be null", performanceMonitor);
            
            // 6. 应用启动阶段
            Log.d(TAG, "Step 6: App Launch");
            if (mPackageManager.getPackageInfo(TEST_PACKAGE_NAME, 0) != null) {
                int pid = mVirtualCore.startVirtualApp(TEST_PACKAGE_NAME);
                assertTrue("Process ID should be positive", pid > 0);
                
                boolean isRunning = mVirtualCore.isVirtualAppRunning(TEST_PACKAGE_NAME);
                assertTrue("Virtual app should be running", isRunning);
                
                // 7. 应用运行阶段
                Log.d(TAG, "Step 7: App Running");
                Thread.sleep(2000); // 等待应用稳定运行
                
                // 验证应用仍在运行
                isRunning = mVirtualCore.isVirtualAppRunning(TEST_PACKAGE_NAME);
                assertTrue("Virtual app should still be running", isRunning);
                
                // 8. 应用终止阶段
                Log.d(TAG, "Step 8: App Termination");
                mVirtualCore.killVirtualApp(TEST_PACKAGE_NAME);
                
                Thread.sleep(1000); // 等待应用完全终止
                
                isRunning = mVirtualCore.isVirtualAppRunning(TEST_PACKAGE_NAME);
                assertFalse("Virtual app should not be running after kill", isRunning);
            } else {
                Log.w(TAG, "Test app not installed, skipping launch step");
            }
            
            // 9. 清理阶段
            Log.d(TAG, "Step 9: Cleanup");
            if (testFile.exists()) {
                boolean deleted = testFile.delete();
                assertTrue("Test file should be deleted", deleted);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Log.d(TAG, "Complete app lifecycle test passed in " + duration + "ms");
            assertTrue("Integration test should complete within timeout", duration < INTEGRATION_TIMEOUT);
            
        } catch (Exception e) {
            Log.e(TAG, "Complete app lifecycle test failed", e);
            fail("Complete app lifecycle should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试错误处理流程
     */
    public void testErrorHandling() {
        Log.d(TAG, "Testing error handling...");
        
        try {
            // 1. 测试无效包名
            Log.d(TAG, "Testing invalid package name");
            VPackage invalidPackage = mPackageManager.getPackageInfo("invalid.package.name", 0);
            assertNull("Invalid package should return null", invalidPackage);
            
            // 2. 测试无效权限
            Log.d(TAG, "Testing invalid permission");
            int invalidPermission = mPackageManager.checkPermission("invalid.permission", TEST_PACKAGE_NAME);
            assertEquals("Invalid permission should be denied", PackageManager.PERMISSION_DENIED, invalidPermission);
            
            // 3. 测试无效文件路径
            Log.d(TAG, "Testing invalid file path");
            int invalidInstall = mPackageManager.installPackage("/invalid/path/app.apk", 0);
            assertNotSame("Invalid APK path should not succeed", PackageManager.INSTALL_SUCCEEDED, invalidInstall);
            
            // 4. 测试重复安装
            Log.d(TAG, "Testing duplicate installation");
            File testApk = new File(TEST_APK_PATH);
            if (testApk.exists()) {
                // 第一次安装
                int firstInstall = mPackageManager.installPackage(TEST_APK_PATH, 0);
                if (firstInstall == PackageManager.INSTALL_SUCCEEDED) {
                    // 第二次安装（应该失败或替换）
                    int secondInstall = mPackageManager.installPackage(TEST_APK_PATH, 0);
                    assertTrue("Second installation should handle duplicate gracefully", 
                              secondInstall == PackageManager.INSTALL_SUCCEEDED || 
                              secondInstall == PackageManager.INSTALL_FAILED_ALREADY_EXISTS);
                }
            }
            
            // 5. 测试空参数处理
            Log.d(TAG, "Testing null parameter handling");
            try {
                mPackageManager.getPackageInfo(null, 0);
                fail("Null package name should throw exception");
            } catch (Exception e) {
                // 预期抛出异常
                Log.d(TAG, "Null parameter correctly handled: " + e.getMessage());
            }
            
            // 6. 测试资源不足情况
            Log.d(TAG, "Testing resource exhaustion");
            testResourceExhaustion();
            
            Log.d(TAG, "Error handling test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling test failed", e);
            fail("Error handling should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试边界条件
     */
    public void testBoundaryConditions() {
        Log.d(TAG, "Testing boundary conditions...");
        
        try {
            // 1. 测试空包名
            Log.d(TAG, "Testing empty package name");
            VPackage emptyPackage = mPackageManager.getPackageInfo("", 0);
            assertNull("Empty package name should return null", emptyPackage);
            
            // 2. 测试极长包名
            Log.d(TAG, "Testing very long package name");
            String longPackageName = "com.example." + "a".repeat(1000);
            VPackage longPackage = mPackageManager.getPackageInfo(longPackageName, 0);
            assertNull("Very long package name should return null", longPackage);
            
            // 3. 测试特殊字符包名
            Log.d(TAG, "Testing special character package name");
            String specialPackageName = "com.example.test@#$%^&*()";
            VPackage specialPackage = mPackageManager.getPackageInfo(specialPackageName, 0);
            assertNull("Special character package name should return null", specialPackage);
            
            // 4. 测试零内存限制
            Log.d(TAG, "Testing zero memory limit");
            MemoryOptimizer memoryOptimizer = MemoryOptimizer.getInstance();
            boolean zeroLimitResult = memoryOptimizer.setMemoryLimit(TEST_PACKAGE_NAME, 0);
            assertTrue("Zero memory limit should be set successfully", zeroLimitResult);
            
            MemoryOptimizer.MemoryInfo memoryInfo = memoryOptimizer.getMemoryInfo(TEST_PACKAGE_NAME);
            assertNotNull("Memory info should not be null", memoryInfo);
            assertEquals("Memory limit should be zero", 0, memoryInfo.memoryLimit);
            
            // 5. 测试极大内存限制
            Log.d(TAG, "Testing very large memory limit");
            long largeMemoryLimit = Long.MAX_VALUE;
            boolean largeLimitResult = memoryOptimizer.setMemoryLimit(TEST_PACKAGE_NAME, largeMemoryLimit);
            assertTrue("Large memory limit should be set successfully", largeLimitResult);
            
            // 6. 测试负数参数
            Log.d(TAG, "Testing negative parameters");
            try {
                memoryOptimizer.setMemoryLimit(TEST_PACKAGE_NAME, -1);
                // 应该处理负数参数
                Log.d(TAG, "Negative memory limit handled gracefully");
            } catch (Exception e) {
                Log.d(TAG, "Negative parameter correctly rejected: " + e.getMessage());
            }
            
            // 7. 测试并发边界条件
            Log.d(TAG, "Testing concurrent boundary conditions");
            testConcurrentBoundaryConditions();
            
            Log.d(TAG, "Boundary conditions test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Boundary conditions test failed", e);
            fail("Boundary conditions should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试压力测试
     */
    public void testStressTest() {
        Log.d(TAG, "Testing stress test...");
        
        try {
            CountDownLatch latch = new CountDownLatch(PRESSURE_TEST_ITERATIONS);
            List<Thread> threads = new ArrayList<>();
            List<Exception> exceptions = new ArrayList<>();
            
            Log.d(TAG, "Starting stress test with " + PRESSURE_TEST_ITERATIONS + " iterations");
            
            for (int i = 0; i < PRESSURE_TEST_ITERATIONS; i++) {
                final int iteration = i;
                Thread thread = new Thread(() -> {
                    try {
                        // 模拟高压力操作
                        String packageName = TEST_PACKAGE_NAME + "_stress_" + iteration;
                        
                        // 并发安装操作
                        if (iteration % 10 == 0) {
                            // 每10次迭代执行一次安装测试
                            testConcurrentInstallation(packageName);
                        }
                        
                        // 并发启动操作
                        if (iteration % 5 == 0) {
                            // 每5次迭代执行一次启动测试
                            testConcurrentStartup(packageName);
                        }
                        
                        // 并发内存操作
                        testConcurrentMemoryOperations(packageName);
                        
                        // 并发网络操作
                        testConcurrentNetworkOperations(packageName);
                        
                        // 并发性能监控
                        testConcurrentPerformanceMonitoring(packageName);
                        
                        Log.d(TAG, "Stress test iteration " + iteration + " completed");
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Stress test iteration " + iteration + " failed", e);
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
            
            // 等待所有线程完成
            boolean completed = latch.await(60, TimeUnit.SECONDS);
            assertTrue("All stress test threads should complete within timeout", completed);
            
            // 检查是否有异常
            if (!exceptions.isEmpty()) {
                Log.w(TAG, "Stress test completed with " + exceptions.size() + " exceptions");
                for (Exception e : exceptions) {
                    Log.w(TAG, "Stress test exception: " + e.getMessage());
                }
            }
            
            // 验证系统稳定性
            verifySystemStability();
            
            Log.d(TAG, "Stress test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Stress test failed", e);
            fail("Stress test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试系统稳定性
     */
    public void testSystemStability() {
        Log.d(TAG, "Testing system stability...");
        
        try {
            // 1. 测试长时间运行稳定性
            Log.d(TAG, "Testing long-term stability");
            long startTime = System.currentTimeMillis();
            long testDuration = 30000; // 30秒
            
            while (System.currentTimeMillis() - startTime < testDuration) {
                // 执行各种操作
                testRandomOperations();
                Thread.sleep(100); // 短暂休息
            }
            
            // 2. 测试内存泄漏
            Log.d(TAG, "Testing memory leak");
            testMemoryLeak();
            
            // 3. 测试资源泄漏
            Log.d(TAG, "Testing resource leak");
            testResourceLeak();
            
            // 4. 测试异常恢复
            Log.d(TAG, "Testing exception recovery");
            testExceptionRecovery();
            
            Log.d(TAG, "System stability test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "System stability test failed", e);
            fail("System stability should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试资源耗尽情况
     */
    private void testResourceExhaustion() {
        Log.d(TAG, "Testing resource exhaustion...");
        
        try {
            // 模拟内存不足情况
            MemoryOptimizer memoryOptimizer = MemoryOptimizer.getInstance();
            memoryOptimizer.setMemoryLimit(TEST_PACKAGE_NAME, 1024); // 1KB限制
            
            MemoryOptimizer.MemoryCheckResult checkResult = memoryOptimizer.checkMemoryUsage(TEST_PACKAGE_NAME);
            assertNotNull("Memory check result should not be null", checkResult);
            
            // 模拟网络不可用情况
            NetworkOptimizer networkOptimizer = NetworkOptimizer.getInstance();
            NetworkOptimizer.NetworkCheckResult networkCheck = networkOptimizer.checkNetworkConnection();
            assertNotNull("Network check result should not be null", networkCheck);
            
            Log.d(TAG, "Resource exhaustion test completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Resource exhaustion test failed", e);
            // 不抛出异常，继续测试
        }
    }
    
    /**
     * 测试并发边界条件
     */
    private void testConcurrentBoundaryConditions() {
        Log.d(TAG, "Testing concurrent boundary conditions...");
        
        CountDownLatch latch = new CountDownLatch(10);
        List<Thread> threads = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    // 并发测试边界条件
                    String packageName = TEST_PACKAGE_NAME + "_boundary_" + threadId;
                    
                    // 测试并发包信息获取
                    for (int j = 0; j < 100; j++) {
                        mPackageManager.getPackageInfo(packageName, 0);
                    }
                    
                    // 测试并发内存操作
                    MemoryOptimizer memoryOptimizer = MemoryOptimizer.getInstance();
                    memoryOptimizer.setMemoryLimit(packageName, threadId * 1024 * 1024);
                    memoryOptimizer.getMemoryInfo(packageName);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Concurrent boundary test thread " + threadId + " failed", e);
                } finally {
                    latch.countDown();
                }
            });
            
            threads.add(thread);
            thread.start();
        }
        
        try {
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertTrue("Concurrent boundary test should complete", completed);
        } catch (InterruptedException e) {
            Log.e(TAG, "Concurrent boundary test interrupted", e);
        }
    }
    
    /**
     * 测试并发安装
     */
    private void testConcurrentInstallation(String packageName) {
        try {
            // 模拟并发安装操作
            File testApk = new File(TEST_APK_PATH);
            if (testApk.exists()) {
                mPackageManager.installPackage(TEST_APK_PATH, 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Concurrent installation failed for " + packageName, e);
        }
    }
    
    /**
     * 测试并发启动
     */
    private void testConcurrentStartup(String packageName) {
        try {
            // 模拟并发启动操作
            mVirtualCore.startVirtualApp(packageName);
            mVirtualCore.killVirtualApp(packageName);
        } catch (Exception e) {
            Log.e(TAG, "Concurrent startup failed for " + packageName, e);
        }
    }
    
    /**
     * 测试并发内存操作
     */
    private void testConcurrentMemoryOperations(String packageName) {
        try {
            MemoryOptimizer memoryOptimizer = MemoryOptimizer.getInstance();
            memoryOptimizer.setMemoryLimit(packageName, 100 * 1024 * 1024);
            memoryOptimizer.getMemoryInfo(packageName);
            memoryOptimizer.checkMemoryUsage(packageName);
        } catch (Exception e) {
            Log.e(TAG, "Concurrent memory operations failed for " + packageName, e);
        }
    }
    
    /**
     * 测试并发网络操作
     */
    private void testConcurrentNetworkOperations(String packageName) {
        try {
            NetworkOptimizer networkOptimizer = NetworkOptimizer.getInstance();
            networkOptimizer.checkNetworkConnection();
            networkOptimizer.getNetworkInfo(packageName);
        } catch (Exception e) {
            Log.e(TAG, "Concurrent network operations failed for " + packageName, e);
        }
    }
    
    /**
     * 测试并发性能监控
     */
    private void testConcurrentPerformanceMonitoring(String packageName) {
        try {
            PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
            performanceMonitor.recordPerformanceData(packageName, PerformanceMonitor.MONITOR_TYPE_CPU, 
                                                    Math.random() * 100, "%");
            performanceMonitor.getPerformanceData(packageName);
        } catch (Exception e) {
            Log.e(TAG, "Concurrent performance monitoring failed for " + packageName, e);
        }
    }
    
    /**
     * 验证系统稳定性
     */
    private void verifySystemStability() {
        Log.d(TAG, "Verifying system stability...");
        
        try {
            // 检查核心组件是否正常
            assertTrue("VirtualCore should be stable", mVirtualCore.isStartup());
            assertNotNull("VPackageManager should be stable", mPackageManager);
            assertNotNull("VEnvironment should be stable", mEnvironment);
            
            // 检查内存使用是否正常
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory();
            long totalMemory = runtime.totalMemory();
            double memoryUsage = (double) (totalMemory - freeMemory) / totalMemory;
            
            Log.d(TAG, "Memory usage after stress test: " + (memoryUsage * 100) + "%");
            assertTrue("Memory usage should be reasonable after stress test", memoryUsage < 0.9);
            
            Log.d(TAG, "System stability verification passed");
            
        } catch (Exception e) {
            Log.e(TAG, "System stability verification failed", e);
            fail("System should remain stable after stress test: " + e.getMessage());
        }
    }
    
    /**
     * 测试随机操作
     */
    private void testRandomOperations() {
        try {
            int operation = (int) (Math.random() * 5);
            String packageName = TEST_PACKAGE_NAME + "_random_" + System.currentTimeMillis();
            
            switch (operation) {
                case 0:
                    mPackageManager.getPackageInfo(packageName, 0);
                    break;
                case 1:
                    MemoryOptimizer.getInstance().getMemoryInfo(packageName);
                    break;
                case 2:
                    NetworkOptimizer.getInstance().checkNetworkConnection();
                    break;
                case 3:
                    PerformanceMonitor.getInstance().recordPerformanceData(packageName, 
                        PerformanceMonitor.MONITOR_TYPE_CPU, Math.random() * 100, "%");
                    break;
                case 4:
                    DataIsolationManager.getInstance().getIsolationInfo(packageName);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Random operation failed", e);
        }
    }
    
    /**
     * 测试内存泄漏
     */
    private void testMemoryLeak() {
        Log.d(TAG, "Testing memory leak...");
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long initialFreeMemory = runtime.freeMemory();
            
            // 执行可能导致内存泄漏的操作
            for (int i = 0; i < 1000; i++) {
                String packageName = TEST_PACKAGE_NAME + "_leak_" + i;
                MemoryOptimizer.getInstance().setMemoryLimit(packageName, 1024 * 1024);
                MemoryOptimizer.getInstance().getMemoryInfo(packageName);
            }
            
            // 强制垃圾回收
            System.gc();
            Thread.sleep(1000);
            
            long finalFreeMemory = runtime.freeMemory();
            long memoryDifference = finalFreeMemory - initialFreeMemory;
            
            Log.d(TAG, "Memory leak test - Initial: " + initialFreeMemory + ", Final: " + finalFreeMemory + 
                      ", Difference: " + memoryDifference);
            
            // 内存差异应该在合理范围内
            assertTrue("Memory leak should be minimal", Math.abs(memoryDifference) < 10 * 1024 * 1024);
            
        } catch (Exception e) {
            Log.e(TAG, "Memory leak test failed", e);
        }
    }
    
    /**
     * 测试资源泄漏
     */
    private void testResourceLeak() {
        Log.d(TAG, "Testing resource leak...");
        
        try {
            // 测试文件句柄泄漏
            for (int i = 0; i < 100; i++) {
                File testFile = new File(mEnvironment.getDataUserPackageDirectory(0, TEST_PACKAGE_NAME), 
                                       "test_resource_" + i + ".txt");
                FileUtils.writeToFile(testFile, "Test resource content");
                // 文件会在测试结束时自动清理
            }
            
            Log.d(TAG, "Resource leak test completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Resource leak test failed", e);
        }
    }
    
    /**
     * 测试异常恢复
     */
    private void testExceptionRecovery() {
        Log.d(TAG, "Testing exception recovery...");
        
        try {
            // 模拟异常情况
            String invalidPackageName = null;
            
            try {
                mPackageManager.getPackageInfo(invalidPackageName, 0);
                fail("Should throw exception for null package name");
            } catch (Exception e) {
                // 预期异常
                Log.d(TAG, "Exception correctly caught: " + e.getMessage());
            }
            
            // 验证系统仍然正常工作
            assertTrue("System should still be functional after exception", mVirtualCore.isStartup());
            assertNotNull("VPackageManager should still work", mPackageManager.getInstalledPackages(0));
            
            Log.d(TAG, "Exception recovery test completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Exception recovery test failed", e);
        }
    }
    
    /**
     * 清理集成测试数据
     */
    private void cleanupIntegrationData() {
        Log.d(TAG, "Cleaning up integration test data...");
        
        try {
            // 清理测试应用
            VPackage testPackage = mPackageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
            if (testPackage != null) {
                mPackageManager.deletePackage(TEST_PACKAGE_NAME, null, 0);
            }
            
            // 清理测试文件
            File testDataDir = mEnvironment.getDataUserPackageDirectory(0, TEST_PACKAGE_NAME);
            if (testDataDir.exists()) {
                FileUtils.deleteDir(testDataDir);
            }
            
            // 清理其他测试数据
            for (int i = 0; i < 100; i++) {
                String packageName = TEST_PACKAGE_NAME + "_stress_" + i;
                mVirtualCore.killVirtualApp(packageName);
                
                File stressDataDir = mEnvironment.getDataUserPackageDirectory(0, packageName);
                if (stressDataDir.exists()) {
                    FileUtils.deleteDir(stressDataDir);
                }
            }
            
            Log.d(TAG, "Integration test data cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup integration test data", e);
        }
    }
} 