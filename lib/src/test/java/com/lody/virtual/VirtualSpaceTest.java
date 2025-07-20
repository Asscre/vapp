package com.lody.virtual;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;
import android.util.Log;

import com.lody.virtual.client.VClient;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VEnvironment;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.pm.VPackageManager;
import com.lody.virtual.pm.parser.VPackage;

import java.io.File;
import java.util.List;

/**
 * 虚拟空间核心功能单元测试
 * 测试应用安装、启动、数据隔离等核心功能
 */
public class VirtualSpaceTest extends AndroidTestCase {
    
    private static final String TAG = "VirtualSpaceTest";
    
    private Context mContext;
    private VirtualCore mVirtualCore;
    private VPackageManager mPackageManager;
    private VEnvironment mEnvironment;
    
    // 测试用的APK文件路径
    private static final String TEST_APK_PATH = "/sdcard/test.apk";
    private static final String TEST_PACKAGE_NAME = "com.example.testapp";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Log.d(TAG, "Setting up VirtualSpaceTest...");
        
        mContext = getContext();
        
        // 初始化虚拟核心
        mVirtualCore = VirtualCore.get();
        assertNotNull("VirtualCore should not be null", mVirtualCore);
        
        // 初始化包管理器
        mPackageManager = VPackageManager.get();
        assertNotNull("VPackageManager should not be null", mPackageManager);
        
        // 初始化环境
        mEnvironment = VEnvironment.get();
        assertNotNull("VEnvironment should not be null", mEnvironment);
        
        Log.d(TAG, "VirtualSpaceTest setup completed");
    }
    
    @Override
    protected void tearDown() throws Exception {
        Log.d(TAG, "Tearing down VirtualSpaceTest...");
        
        // 清理测试数据
        cleanupTestData();
        
        super.tearDown();
    }
    
    /**
     * 测试虚拟核心初始化
     */
    public void testVirtualCoreInitialization() {
        Log.d(TAG, "Testing VirtualCore initialization...");
        
        // 测试虚拟核心是否已初始化
        assertTrue("VirtualCore should be initialized", mVirtualCore.isStartup());
        
        // 测试虚拟核心版本
        String version = mVirtualCore.getVersionName();
        assertNotNull("Version should not be null", version);
        assertFalse("Version should not be empty", version.isEmpty());
        
        Log.d(TAG, "VirtualCore version: " + version);
        
        // 测试虚拟核心是否支持当前设备
        assertTrue("VirtualCore should support current device", mVirtualCore.isVAppInstalled());
        
        Log.d(TAG, "VirtualCore initialization test passed");
    }
    
    /**
     * 测试环境初始化
     */
    public void testEnvironmentInitialization() {
        Log.d(TAG, "Testing VEnvironment initialization...");
        
        // 测试数据目录
        File dataDir = mEnvironment.getDataUserPackageDirectory(0, TEST_PACKAGE_NAME);
        assertNotNull("Data directory should not be null", dataDir);
        
        // 测试缓存目录
        File cacheDir = mEnvironment.getCacheDir();
        assertNotNull("Cache directory should not be null", cacheDir);
        
        // 测试外部存储目录
        File externalDir = mEnvironment.getExternalStorageDirectory();
        assertNotNull("External storage directory should not be null", externalDir);
        
        // 测试库目录
        File libDir = mEnvironment.getLibraryDirectory();
        assertNotNull("Library directory should not be null", libDir);
        
        Log.d(TAG, "VEnvironment initialization test passed");
    }
    
    /**
     * 测试包管理器功能
     */
    public void testPackageManager() {
        Log.d(TAG, "Testing VPackageManager functionality...");
        
        // 测试获取已安装应用列表
        List<VPackage> installedPackages = mPackageManager.getInstalledPackages(0);
        assertNotNull("Installed packages list should not be null", installedPackages);
        
        Log.d(TAG, "Found " + installedPackages.size() + " installed packages");
        
        // 测试获取系统应用列表
        List<VPackage> systemPackages = mPackageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        assertNotNull("System packages list should not be null", systemPackages);
        
        Log.d(TAG, "Found " + systemPackages.size() + " system packages");
        
        // 测试包信息获取
        for (VPackage pkg : installedPackages) {
            assertNotNull("Package name should not be null", pkg.packageName);
            assertNotNull("Application info should not be null", pkg.applicationInfo);
            
            // 测试获取包信息
            VPackage packageInfo = mPackageManager.getPackageInfo(pkg.packageName, 0);
            assertNotNull("Package info should not be null", packageInfo);
            assertEquals("Package names should match", pkg.packageName, packageInfo.packageName);
        }
        
        Log.d(TAG, "VPackageManager functionality test passed");
    }
    
    /**
     * 测试应用安装功能
     */
    public void testAppInstallation() {
        Log.d(TAG, "Testing app installation...");
        
        // 检查测试APK是否存在
        File testApk = new File(TEST_APK_PATH);
        if (!testApk.exists()) {
            Log.w(TAG, "Test APK not found, skipping installation test");
            return;
        }
        
        try {
            // 测试安装APK
            int result = mPackageManager.installPackage(TEST_APK_PATH, 0);
            assertEquals("Installation should succeed", PackageManager.INSTALL_SUCCEEDED, result);
            
            // 验证应用已安装
            VPackage packageInfo = mPackageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
            assertNotNull("Installed package info should not be null", packageInfo);
            assertEquals("Package name should match", TEST_PACKAGE_NAME, packageInfo.packageName);
            
            Log.d(TAG, "App installation test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "App installation test failed", e);
            fail("App installation should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试应用卸载功能
     */
    public void testAppUninstallation() {
        Log.d(TAG, "Testing app uninstallation...");
        
        try {
            // 检查应用是否已安装
            VPackage packageInfo = mPackageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
            if (packageInfo == null) {
                Log.w(TAG, "Test app not installed, skipping uninstallation test");
                return;
            }
            
            // 测试卸载应用
            int result = mPackageManager.deletePackage(TEST_PACKAGE_NAME, null, 0);
            assertEquals("Uninstallation should succeed", PackageManager.DELETE_SUCCEEDED, result);
            
            // 验证应用已卸载
            VPackage uninstalledPackage = mPackageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
            assertNull("Uninstalled package should be null", uninstalledPackage);
            
            Log.d(TAG, "App uninstallation test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "App uninstallation test failed", e);
            fail("App uninstallation should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试数据隔离功能
     */
    public void testDataIsolation() {
        Log.d(TAG, "Testing data isolation...");
        
        String testPackageName = "com.test.isolation";
        
        try {
            // 创建测试数据目录
            File dataDir = mEnvironment.getDataUserPackageDirectory(0, testPackageName);
            assertNotNull("Data directory should not be null", dataDir);
            
            // 创建测试文件
            File testFile = new File(dataDir, "test.txt");
            String testContent = "Test data isolation content";
            FileUtils.writeToFile(testFile, testContent);
            
            // 验证文件已创建
            assertTrue("Test file should exist", testFile.exists());
            assertEquals("File content should match", testContent, FileUtils.readToString(testFile));
            
            // 测试不同用户的数据隔离
            File user1DataDir = mEnvironment.getDataUserPackageDirectory(1, testPackageName);
            File user1TestFile = new File(user1DataDir, "test.txt");
            
            // 用户1的文件应该不存在
            assertFalse("User 1 test file should not exist", user1TestFile.exists());
            
            // 创建用户1的测试文件
            String user1Content = "User 1 test content";
            FileUtils.writeToFile(user1TestFile, user1Content);
            
            // 验证两个用户的文件内容不同
            assertNotSame("File contents should be different", 
                         FileUtils.readToString(testFile), 
                         FileUtils.readToString(user1TestFile));
            
            Log.d(TAG, "Data isolation test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Data isolation test failed", e);
            fail("Data isolation should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试权限控制功能
     */
    public void testPermissionControl() {
        Log.d(TAG, "Testing permission control...");
        
        try {
            // 测试权限检查
            boolean hasInternetPermission = mPackageManager.checkPermission(
                android.Manifest.permission.INTERNET, TEST_PACKAGE_NAME);
            
            // 权限检查应该返回有效结果
            assertTrue("Permission check should return valid result", 
                      hasInternetPermission == PackageManager.PERMISSION_GRANTED || 
                      hasInternetPermission == PackageManager.PERMISSION_DENIED);
            
            // 测试权限授予
            mPackageManager.grantPermission(TEST_PACKAGE_NAME, android.Manifest.permission.INTERNET);
            
            // 验证权限已授予
            boolean grantedPermission = mPackageManager.checkPermission(
                android.Manifest.permission.INTERNET, TEST_PACKAGE_NAME);
            assertEquals("Permission should be granted", PackageManager.PERMISSION_GRANTED, grantedPermission);
            
            // 测试权限撤销
            mPackageManager.revokePermission(TEST_PACKAGE_NAME, android.Manifest.permission.INTERNET);
            
            // 验证权限已撤销
            boolean revokedPermission = mPackageManager.checkPermission(
                android.Manifest.permission.INTERNET, TEST_PACKAGE_NAME);
            assertEquals("Permission should be revoked", PackageManager.PERMISSION_DENIED, revokedPermission);
            
            Log.d(TAG, "Permission control test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Permission control test failed", e);
            fail("Permission control should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试进程管理功能
     */
    public void testProcessManagement() {
        Log.d(TAG, "Testing process management...");
        
        try {
            // 测试进程创建
            int pid = mVirtualCore.startVirtualApp(TEST_PACKAGE_NAME);
            assertTrue("Process ID should be positive", pid > 0);
            
            // 测试进程是否运行
            boolean isRunning = mVirtualCore.isVirtualAppRunning(TEST_PACKAGE_NAME);
            assertTrue("Virtual app should be running", isRunning);
            
            // 测试获取运行中的应用列表
            List<String> runningApps = mVirtualCore.getRunningVirtualApps();
            assertNotNull("Running apps list should not be null", runningApps);
            assertTrue("Running apps should contain test app", runningApps.contains(TEST_PACKAGE_NAME));
            
            // 测试进程终止
            mVirtualCore.killVirtualApp(TEST_PACKAGE_NAME);
            
            // 验证进程已终止
            boolean isStillRunning = mVirtualCore.isVirtualAppRunning(TEST_PACKAGE_NAME);
            assertFalse("Virtual app should not be running after kill", isStillRunning);
            
            Log.d(TAG, "Process management test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Process management test failed", e);
            fail("Process management should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试Hook框架功能
     */
    public void testHookFramework() {
        Log.d(TAG, "Testing Hook framework...");
        
        try {
            // 测试Hook是否已初始化
            boolean isHookInitialized = mVirtualCore.isHookEnabled();
            assertTrue("Hook framework should be initialized", isHookInitialized);
            
            // 测试系统API Hook
            boolean isSystemApiHooked = mVirtualCore.isSystemApiHooked();
            assertTrue("System API should be hooked", isSystemApiHooked);
            
            Log.d(TAG, "Hook framework test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Hook framework test failed", e);
            fail("Hook framework should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试性能优化功能
     */
    public void testPerformanceOptimization() {
        Log.d(TAG, "Testing performance optimization...");
        
        try {
            // 测试启动优化器
            com.lody.virtual.optimization.StartupOptimizer startupOptimizer = 
                com.lody.virtual.optimization.StartupOptimizer.getInstance();
            assertNotNull("StartupOptimizer should not be null", startupOptimizer);
            
            // 测试内存优化器
            com.lody.virtual.optimization.MemoryOptimizer memoryOptimizer = 
                com.lody.virtual.optimization.MemoryOptimizer.getInstance();
            assertNotNull("MemoryOptimizer should not be null", memoryOptimizer);
            
            // 测试网络优化器
            com.lody.virtual.optimization.NetworkOptimizer networkOptimizer = 
                com.lody.virtual.optimization.NetworkOptimizer.getInstance();
            assertNotNull("NetworkOptimizer should not be null", networkOptimizer);
            
            // 测试性能监控器
            com.lody.virtual.optimization.PerformanceMonitor performanceMonitor = 
                com.lody.virtual.optimization.PerformanceMonitor.getInstance();
            assertNotNull("PerformanceMonitor should not be null", performanceMonitor);
            
            Log.d(TAG, "Performance optimization test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Performance optimization test failed", e);
            fail("Performance optimization should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试安全机制功能
     */
    public void testSecurityMechanism() {
        Log.d(TAG, "Testing security mechanism...");
        
        try {
            // 测试数据隔离管理器
            com.lody.virtual.security.DataIsolationManager dataIsolationManager = 
                com.lody.virtual.security.DataIsolationManager.getInstance();
            assertNotNull("DataIsolationManager should not be null", dataIsolationManager);
            
            // 测试网络隔离管理器
            com.lody.virtual.security.NetworkIsolationManager networkIsolationManager = 
                com.lody.virtual.security.NetworkIsolationManager.getInstance();
            assertNotNull("NetworkIsolationManager should not be null", networkIsolationManager);
            
            // 测试权限控制管理器
            com.lody.virtual.security.PermissionControlManager permissionControlManager = 
                com.lody.virtual.security.PermissionControlManager.getInstance();
            assertNotNull("PermissionControlManager should not be null", permissionControlManager);
            
            // 测试数据加密管理器
            com.lody.virtual.security.DataEncryptionManager dataEncryptionManager = 
                com.lody.virtual.security.DataEncryptionManager.getInstance();
            assertNotNull("DataEncryptionManager should not be null", dataEncryptionManager);
            
            Log.d(TAG, "Security mechanism test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Security mechanism test failed", e);
            fail("Security mechanism should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 测试错误处理功能
     */
    public void testErrorHandling() {
        Log.d(TAG, "Testing error handling...");
        
        try {
            // 测试无效包名
            VPackage invalidPackage = mPackageManager.getPackageInfo("invalid.package.name", 0);
            assertNull("Invalid package should return null", invalidPackage);
            
            // 测试无效权限
            int invalidPermission = mPackageManager.checkPermission("invalid.permission", TEST_PACKAGE_NAME);
            assertEquals("Invalid permission should be denied", PackageManager.PERMISSION_DENIED, invalidPermission);
            
            // 测试无效文件路径
            int invalidInstall = mPackageManager.installPackage("/invalid/path/app.apk", 0);
            assertNotSame("Invalid APK path should not succeed", PackageManager.INSTALL_SUCCEEDED, invalidInstall);
            
            Log.d(TAG, "Error handling test passed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling test failed", e);
            fail("Error handling should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        Log.d(TAG, "Cleaning up test data...");
        
        try {
            // 清理测试应用
            VPackage testPackage = mPackageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
            if (testPackage != null) {
                mPackageManager.deletePackage(TEST_PACKAGE_NAME, null, 0);
            }
            
            // 清理测试文件
            File testDataDir = mEnvironment.getDataUserPackageDirectory(0, "com.test.isolation");
            if (testDataDir.exists()) {
                FileUtils.deleteDir(testDataDir);
            }
            
            Log.d(TAG, "Test data cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup test data", e);
        }
    }
} 