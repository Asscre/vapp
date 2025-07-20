package com.lody.virtual;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.lody.virtual.security.DataIsolationManager;
import com.lody.virtual.security.PermissionControlManager;
import com.lody.virtual.security.DataEncryptionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 虚拟化引擎核心类
 * 负责管理虚拟化环境、应用安装、进程管理等核心功能
 */
public class VirtualCore {
    
    private static final String TAG = "VirtualCore";
    private static VirtualCore sInstance;
    
    private Context mContext;
    private boolean mIsInitialized = false;
    private VEnvironment mEnvironment;
    private ProcessManager mProcessManager;
    private VPackageManager mPackageManager;
    private DataIsolationManager mDataIsolationManager;
    private PermissionControlManager mPermissionManager;
    private DataEncryptionManager mDataEncryptionManager;
    
    // 虚拟应用缓存
    private final ConcurrentHashMap<String, VAppInfo> mVirtualApps = new ConcurrentHashMap<>();
    
    private VirtualCore() {
        // 私有构造函数，实现单例模式
    }
    
    public static VirtualCore getInstance() {
        if (sInstance == null) {
            synchronized (VirtualCore.class) {
                if (sInstance == null) {
                    sInstance = new VirtualCore();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 初始化虚拟化引擎
     * @param context 应用上下文
     * @return 初始化是否成功
     */
    public boolean initialize(Context context) {
        if (mIsInitialized) {
            Log.w(TAG, "VirtualCore already initialized");
            return true;
        }
        
        try {
            Log.d(TAG, "Initializing VirtualCore...");
            
            mContext = context.getApplicationContext();
            
            // 初始化环境管理器
            mEnvironment = new VEnvironment(mContext);
            mEnvironment.initialize();
            
            // 初始化进程管理器
            mProcessManager = ProcessManager.getInstance();
            mProcessManager.initialize(mContext);
            
            // 初始化包管理器
            mPackageManager = VPackageManager.getInstance();
            mPackageManager.initialize(mContext, this);
            
            // 初始化数据隔离管理器
            mDataIsolationManager = DataIsolationManager.getInstance();
            mDataIsolationManager.initialize(mContext, this, mEnvironment);
            
            // 初始化权限管理器
            mPermissionManager = PermissionControlManager.getInstance();
            mPermissionManager.initialize(mContext, this);
            
            // 初始化数据加密管理器
            mDataEncryptionManager = DataEncryptionManager.getInstance();
            mDataEncryptionManager.initialize(mContext, this);
            
            // 加载已安装的虚拟应用
            loadVirtualApps();
            
            mIsInitialized = true;
            Log.d(TAG, "VirtualCore initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize VirtualCore", e);
            return false;
        }
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    /**
     * 获取应用上下文
     */
    public Context getContext() {
        return mContext;
    }
    
    /**
     * 获取环境管理器
     */
    public VEnvironment getEnvironment() {
        return mEnvironment;
    }
    
    /**
     * 获取进程管理器
     */
    public ProcessManager getProcessManager() {
        return mProcessManager;
    }
    
    /**
     * 获取包管理器
     */
    public VPackageManager getPackageManager() {
        return mPackageManager;
    }
    
    /**
     * 获取数据隔离管理器
     */
    public DataIsolationManager getDataIsolationManager() {
        return mDataIsolationManager;
    }
    
    /**
     * 获取权限管理器
     */
    public PermissionControlManager getPermissionManager() {
        return mPermissionManager;
    }
    
    /**
     * 获取数据加密管理器
     */
    public DataEncryptionManager getDataEncryptionManager() {
        return mDataEncryptionManager;
    }
    
    /**
     * 安装虚拟应用
     * @param apkPath APK文件路径
     * @return 安装结果
     */
    public InstallResult installVirtualApp(String apkPath) {
        if (!mIsInitialized) {
            Log.e(TAG, "VirtualCore not initialized");
            return new InstallResult(false, "VirtualCore not initialized");
        }
        
        try {
            Log.d(TAG, "Installing virtual app: " + apkPath);
            
            // 检查APK文件是否存在
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) {
                return new InstallResult(false, "APK file not found: " + apkPath);
            }
            
            // 使用包管理器安装应用
            VPackageManager.InstallResult result = mPackageManager.installVirtualApp(apkPath);
            
            // 转换为VirtualCore的InstallResult
            return new InstallResult(result.success, result.message);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to install virtual app", e);
            return new InstallResult(false, "Installation failed: " + e.getMessage());
        }
    }
    
    /**
     * 卸载虚拟应用
     * @param packageName 包名
     * @return 卸载是否成功
     */
    public boolean uninstallVirtualApp(String packageName) {
        if (!mIsInitialized) {
            Log.e(TAG, "VirtualCore not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Uninstalling virtual app: " + packageName);
            
            // 使用包管理器卸载应用
            boolean success = mPackageManager.uninstallVirtualApp(packageName);
            
            if (success) {
                // 从缓存中移除
                mVirtualApps.remove(packageName);
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to uninstall virtual app", e);
            return false;
        }
    }
    
    /**
     * 启动虚拟应用
     * @param packageName 包名
     * @return 启动结果
     */
    public StartProcessResult startVirtualApp(String packageName) {
        if (!mIsInitialized) {
            Log.e(TAG, "VirtualCore not initialized");
            return new StartProcessResult(false, "VirtualCore not initialized");
        }
        
        try {
            Log.d(TAG, "Starting virtual app: " + packageName);
            
            // 检查应用是否已安装
            VAppInfo appInfo = mVirtualApps.get(packageName);
            if (appInfo == null) {
                return new StartProcessResult(false, "App not installed: " + packageName);
            }
            
            // 使用进程管理器启动应用
            ProcessManager.StartProcessResult result = mProcessManager.startVirtualProcess(appInfo);
            
            // 转换为VirtualCore的StartProcessResult
            return new StartProcessResult(result.success, result.message, result.processId);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start virtual app", e);
            return new StartProcessResult(false, "Start failed: " + e.getMessage());
        }
    }
    
    /**
     * 停止虚拟应用
     * @param packageName 包名
     * @return 停止是否成功
     */
    public boolean stopVirtualApp(String packageName) {
        if (!mIsInitialized) {
            Log.e(TAG, "VirtualCore not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Stopping virtual app: " + packageName);
            
            // 使用进程管理器停止应用
            return mProcessManager.stopVirtualProcess(packageName);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop virtual app", e);
            return false;
        }
    }
    
    /**
     * 获取虚拟应用信息
     * @param packageName 包名
     * @return 应用信息
     */
    public VAppInfo getVirtualAppInfo(String packageName) {
        if (!mIsInitialized) {
            return null;
        }
        
        return mVirtualApps.get(packageName);
    }
    
    /**
     * 获取所有虚拟应用
     * @return 应用列表
     */
    public List<VAppInfo> getAllVirtualApps() {
        if (!mIsInitialized) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(mVirtualApps.values());
    }
    
    /**
     * 检查是否是虚拟应用
     * @param packageName 包名
     * @return 是否是虚拟应用
     */
    public boolean isVirtualApp(String packageName) {
        if (!mIsInitialized) {
            return false;
        }
        
        return mVirtualApps.containsKey(packageName);
    }
    
    /**
     * 加载已安装的虚拟应用
     */
    private void loadVirtualApps() {
        try {
            Log.d(TAG, "Loading virtual apps...");
            
            // 从包管理器获取已安装的应用
            List<VAppInfo> apps = mPackageManager.getAllVirtualApps();
            
            // 添加到缓存
            for (VAppInfo app : apps) {
                mVirtualApps.put(app.packageName, app);
            }
            
            Log.d(TAG, "Loaded " + apps.size() + " virtual apps");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load virtual apps", e);
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (!mIsInitialized) {
            return;
        }
        
        try {
            Log.d(TAG, "Cleaning up VirtualCore...");
            
            // 清理进程管理器
            if (mProcessManager != null) {
                mProcessManager.cleanup();
            }
            
            // 清理包管理器
            if (mPackageManager != null) {
                mPackageManager.cleanup();
            }
            
            // 清理数据隔离管理器
            if (mDataIsolationManager != null) {
                mDataIsolationManager.cleanup();
            }
            
            // 清理权限管理器
            if (mPermissionManager != null) {
                mPermissionManager.cleanup();
            }
            
            // 清理数据加密管理器
            if (mDataEncryptionManager != null) {
                mDataEncryptionManager.cleanup();
            }
            
            // 清理虚拟应用缓存
            mVirtualApps.clear();
            
            mIsInitialized = false;
            Log.d(TAG, "VirtualCore cleaned up successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup VirtualCore", e);
        }
    }
    
    // 网络相关方法
    public boolean checkNetworkPermission(String host, int port) {
        // 暂时返回true，表示允许网络访问
        return true;
    }
    
    public String getVirtualNetworkHost(String host) {
        // 暂时返回原主机名
        return host;
    }
    
    public String getVirtualNetworkUrl(String url) {
        // 暂时返回原URL
        return url;
    }
    
    // 包管理相关方法
    public PackageInfo getVirtualPackageInfo(String packageName) {
        if (mPackageManager != null) {
            return mPackageManager.getVirtualPackageInfo(packageName);
        }
        return null;
    }
    
    public Object getVirtualInstalledPackages(int flags) {
        if (mPackageManager != null) {
            return mPackageManager.getVirtualInstalledPackages(flags);
        }
        return new ArrayList<>();
    }
    
    public Object resolveVirtualActivity(Object intent, int flags) {
        if (mPackageManager != null) {
            return mPackageManager.resolveVirtualActivity((android.content.Intent) intent, flags);
        }
        return null;
    }
    
    public Object queryVirtualIntentActivities(Object intent, int flags) {
        if (mPackageManager != null) {
            return mPackageManager.queryVirtualIntentActivities((android.content.Intent) intent, flags);
        }
        return new ArrayList<>();
    }
    
    public Object queryVirtualIntentServices(Object intent, int flags) {
        if (mPackageManager != null) {
            return mPackageManager.queryVirtualIntentServices((android.content.Intent) intent, flags);
        }
        return new ArrayList<>();
    }
    
    public int checkVirtualPermission(String permission, String packageName) {
        if (mPermissionManager != null) {
            return mPermissionManager.checkVirtualPermission(permission, packageName);
        }
        return PackageManager.PERMISSION_DENIED;
    }
    
    public Object getVirtualApplicationInfo(String packageName, int flags) {
        if (mPackageManager != null) {
            return mPackageManager.getVirtualApplicationInfo(packageName, flags);
        }
        return null;
    }
    
    // 设备信息相关方法
    public String getVirtualDeviceModel() {
        return Build.MODEL;
    }
    
    public String getVirtualDeviceManufacturer() {
        return Build.MANUFACTURER;
    }
    
    public String getVirtualDeviceBrand() {
        return Build.BRAND;
    }
    
    public String getVirtualDeviceProduct() {
        return Build.PRODUCT;
    }
    
    public String getVirtualDeviceName() {
        return Build.DEVICE;
    }
    
    public String getVirtualDeviceFingerprint() {
        return Build.FINGERPRINT;
    }
    
    public String getVirtualDeviceSerial() {
        return Build.SERIAL;
    }
    
    public String getVirtualDeviceHardware() {
        return Build.HARDWARE;
    }
    
    public String getVirtualBuildHost() {
        return Build.HOST;
    }
    
    public String getVirtualBuildTags() {
        return Build.TAGS;
    }
    
    public String getVirtualBuildType() {
        return Build.TYPE;
    }
    
    public String getVirtualBuildUser() {
        return Build.USER;
    }
    
    // 进程相关方法
    public int getVirtualProcessId() {
        return android.os.Process.myPid();
    }
    
    public int getVirtualUserId() {
        return android.os.Process.myUid();
    }
    
    public Object getVirtualUserHandle() {
        return android.os.Process.myUserHandle();
    }
    
    public boolean isVirtualProcess(int pid) {
        if (mProcessManager != null) {
            return mProcessManager.isVirtualProcess(pid);
        }
        return false;
    }
    
    public void killVirtualProcess(int pid) {
        if (mProcessManager != null) {
            mProcessManager.killVirtualProcess(pid);
        }
    }
    
    public boolean isVirtualThread(int tid) {
        // 暂时返回false
        return false;
    }
    
    public void setVirtualThreadPriority(int tid, int priority) {
        // 暂时不实现
    }
    
    public int getVirtualThreadPriority(int tid) {
        // 暂时返回默认优先级
        return android.os.Process.THREAD_PRIORITY_DEFAULT;
    }
    
    /**
     * 安装结果类
     */
    public static class InstallResult {
        public final boolean success;
        public final String message;
        
        public InstallResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
    
    /**
     * 启动结果类
     */
    public static class StartProcessResult {
        public final boolean success;
        public final String message;
        public final int processId;
        
        public StartProcessResult(boolean success, String message) {
            this(success, message, -1);
        }
        
        public StartProcessResult(boolean success, String message, int processId) {
            this.success = success;
            this.message = message;
            this.processId = processId;
        }
    }
} 